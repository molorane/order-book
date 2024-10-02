package com.valr.order_book.service.impl

import com.valr.order_book.entity.Trade
import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.Status
import com.valr.order_book.repository.OrderRepository
import com.valr.order_book.repository.TradeRepository
import com.valr.order_book.service.OrderQueue
import com.valr.order_book.service.TradeWorkerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TradeWorkerServiceImpl(
    private val orderQueue: OrderQueue,
    private val tradeRepository: TradeRepository,
    private val orderRepository: OrderRepository
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

        // save a new trade in a DB
        tradeRepository.save(newTrade)

        // update sell order
        updateTrade(sellOrder, matchedQuantity)

        // update buy order
        updateTrade(buyOrder, matchedQuantity)
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
            // This order may likely be executed by a different worker thread
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