package com.valr.order_book.service.impl


import com.valr.order_book.entity.Trade
import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.UserWallet
import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.FlowType
import com.valr.order_book.entity.enums.Status
import com.valr.order_book.repository.OrderRepository
import com.valr.order_book.repository.TradeRepository
import com.valr.order_book.repository.UserWalletRepository
import com.valr.order_book.service.OrderQueue
import com.valr.order_book.service.TradeWorkerService
import com.valr.order_book.util.matchBuyCurrency
import com.valr.order_book.util.matchSellCurrency
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TradeWorkerServiceImpl(
    private val orderQueue: OrderQueue,
    private val tradeRepository: TradeRepository,
    private val orderRepository: OrderRepository,
    private val userWalletRepository: UserWalletRepository
) : TradeWorkerService {
    private val logger = LoggerFactory.getLogger(TradeWorkerServiceImpl::class.java)

    @Transactional
    override fun executeTrade(sellOrder: TradeOrder, buyOrder: TradeOrder) {
        logger.info("Matched Trade -> SELL Order: $sellOrder with BUY Order: $buyOrder")

        val matchedPrice = sellOrder.price // lowest sell price

        val matchedQuantity = sellOrder.quantity.minus(sellOrder.matchedQuantity).min(buyOrder.quantity)

        // create a new trade entry
        val newTrade = Trade(
            seller = sellOrder.copy(),
            buyer = buyOrder.copy(),
            quantity = matchedQuantity,
            price = matchedPrice,
            tradedAt = LocalDateTime.now()
        )

        // save a new trade into a DB
        tradeRepository.save(newTrade)

        // update user wallet
        updateUserWaller(newTrade, sellOrder.currencyPair)

        // update sell order
        updateTrade(sellOrder, matchedQuantity)

        // update buy order
        updateTrade(buyOrder, matchedQuantity)
    }

    // Successful trade entails four records in user wallet
    // Two for a seller, and two for a buyer
    private fun updateUserWaller(newTrade: Trade, currencyPair: CurrencyPair) {
        val sellCurrency = matchSellCurrency(currencyPair);
        val buyCurrency = matchBuyCurrency(currencyPair);

        val sellerWalletOut = UserWallet(
            currency = sellCurrency,
            flowType = FlowType.OUT,
            quantity = newTrade.quantity,
            user = newTrade.seller?.user
        )

        val sellerWalletIn = UserWallet(
            currency = buyCurrency,
            flowType = FlowType.IN,
            quantity = newTrade.quantity,
            user = newTrade.seller?.user
        )
        userWalletRepository.save(sellerWalletIn)
        userWalletRepository.save(sellerWalletOut)

        val buyerWalletIn = UserWallet(
            currency = sellCurrency,
            flowType = FlowType.IN,
            quantity = newTrade.quantity,
            user = newTrade.buyer?.user
        )

        val buyerWalletOut = UserWallet(
            currency = buyCurrency,
            flowType = FlowType.OUT,
            quantity = newTrade.quantity,
            user = newTrade.buyer?.user
        )

        userWalletRepository.save(buyerWalletIn)
        userWalletRepository.save(buyerWalletOut)
    }

    private fun updateTrade(order: TradeOrder, matchedQuantity: BigDecimal) {
        // Determine if an order is FILLED
        if (order.quantity.minus(order.matchedQuantity) > matchedQuantity) {
            val newOrder = order.copy(
                status = Status.PARTIALLY_FILLED,
                matchedQuantity = order.matchedQuantity.plus(matchedQuantity)
            )

            // update order
            orderRepository.save(newOrder)

            // Since the order is not FILLED, take PARTIALLY FILLED order back to the queue for further processing until it is filled
            // Order becomes FILLED(completed) when matchedQuantity = quantity
            // This order may be executed by a different worker thread
            orderQueue.addOrder(newOrder)
        } else {
            val newOrder = order.copy(
                status = Status.FILLED,
                matchedQuantity = order.quantity
            )

            // update order
            orderRepository.save(newOrder)
        }
    }
}