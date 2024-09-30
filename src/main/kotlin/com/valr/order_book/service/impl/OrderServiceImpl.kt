package com.valr.order_book.service.impl

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.enums.Currency
import com.valr.order_book.entity.enums.TakerSide
import com.valr.order_book.exception.InsufficientFundsException
import com.valr.order_book.exception.InvalidOrderException
import com.valr.order_book.mapper.CurrencyPairMapper
import com.valr.order_book.mapper.TradeOrderMapper
import com.valr.order_book.model.*
import com.valr.order_book.repository.OrderRepository
import com.valr.order_book.repository.UserRepository
import com.valr.order_book.repository.UserWalletRepository
import com.valr.order_book.service.OrderService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val userWalletRepository: UserWalletRepository
) : OrderService {

    // Sell orders (min-heap for lowest price)
    private val sellOrders: PriorityQueue<TradeOrder> =
        PriorityQueue<TradeOrder> { a: TradeOrder, b: TradeOrder ->
            a.price.compareTo(b.price)
        }

    // Buy orders (max-heap for highest price)
    private val buyOrders: PriorityQueue<TradeOrder> =
        PriorityQueue<TradeOrder> { a: TradeOrder, b: TradeOrder ->
            b.price.compareTo(a.price)
        }


    /*
        A valid order request object must have
        Price <=0 and Quality <=0
        QuoteVolume >= 10
    */
    override fun validOrder(orderRequest: OrderRequestDto): Boolean {
        return orderRequest.price!! > BigDecimal.ZERO || orderRequest.quantity!! > BigDecimal.ZERO ||
                (orderRequest.price.multiply(orderRequest.quantity) >= BigDecimal("10"))
    }

    // This is a pseudo-method, this can be read from appropriate table of currencies from storage/API
    override fun matchBuyCurrency(currency: CurrencyPairDto): Currency {
        return when (currency) {
            CurrencyPairDto.XRPZAR, CurrencyPairDto.BTCZAR -> Currency.ZAR
            else -> throw InvalidOrderException("Invalid currency pair")
        }
    }

    // This is a pseudo-method, this can be read from appropriate table of currencies from storage/API
    override fun matchSellCurrency(currency: CurrencyPairDto): Currency {
        return when (currency) {
            CurrencyPairDto.XRPZAR -> Currency.XRP
            CurrencyPairDto.BTCZAR -> Currency.BTC
            else -> throw InvalidOrderException("Invalid currency pair")
        }
    }

    /*
        Validate that user has sufficient funds to place an order
        I think here we first get user's wallets, then check the corresponding wallet
        to validate that the user has sufficient funds to place an order
        Also, I thought of a situation where a user has placed an order, but it has not been executed
        E.g User placed a SELL order of 100 XRPs, then places another SELL order of 20 XRPs while the previous order is still open
    */
    override fun fundsAvailable(userId: Long, orderRequest: OrderRequestDto): Boolean {
        val user = userRepository.findUserWithWallets(userId)
        if (!user.isPresent) {
            return false
        }

        val balance = userWalletRepository.walletBalance(
            userId,
            if (orderRequest.side == SideDto.BUY) matchBuyCurrency(orderRequest.pair!!)
            else matchSellCurrency(orderRequest.pair!!)
        )

        if (balance.isEmpty) {
            return false
        }

        val volume = orderRequest.quantity?.multiply(orderRequest.price)

        return if (orderRequest.side == SideDto.BUY)
            balance.get().getQuantityDifference() >= volume
        else
            orderRequest.quantity!! < balance.get().getQuantityDifference()
    }

    override fun placeOrder(userId: Long, orderRequest: OrderRequestDto): OrderResponseDto {

        // Step 1: Ensure the order request object is valid
        if (!validOrder(orderRequest)) {
            throw InvalidOrderException("Invalid order request.")
        }

        // Step 2: Ensure the user has sufficient funds in a wallet to place an order
        if (!fundsAvailable(userId, orderRequest)) {
            throw InsufficientFundsException("Insufficient balance.")
        }

        // Step 3: process the order
        val orderObj = TradeOrderMapper.INSTANCE.requestToInternal(orderRequest)

        // Record this order in a DB
        val newOrder = orderRepository.save(orderObj)

        // Put new order in a queue for processing
        // My thinking is that, newOrder should most likely be sent to Cache(Redis, Memcached etc) for global access
        // so that other running instances can access it and perhaps process it
        // Running instances since they are running concurrently, there must be thread safety to ensure correct order processing
        if (orderObj.takerSide == TakerSide.BUY) {
            buyOrders.add(newOrder)
        } else {
            sellOrders.add(newOrder)
        }

        return TradeOrderMapper.INSTANCE.internalToOrderResponse(newOrder)
    }

    override fun orderBook(currencyPair: CurrencyPairDto): OrderBookDto {

        val orderBook = orderRepository.orderBook(
            CurrencyPairMapper.INSTANCE.dtoToInternal(currencyPair)
        )
            .stream()
            .map { trade -> TradeOrderMapper.INSTANCE.orderBookProjection(trade) }
            .collect(Collectors.groupingBy(OrderDto::side))

        val asks = orderBook[SideDto.SELL]?.toMutableList()
        val bids = orderBook[SideDto.BUY]?.toMutableList()

        asks?.sortWith(compareBy<OrderDto> { it.price })
        bids?.sortByDescending { it.price }

        return OrderBookDto(
            asks = asks ?: emptyList(),
            bids = bids ?: emptyList(),
            lastChange = LocalDateTime.now(),
        )
    }
}