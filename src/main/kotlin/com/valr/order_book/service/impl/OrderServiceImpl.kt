package com.valr.order_book.service.impl

import com.valr.order_book.entity.enums.Currency
import com.valr.order_book.exception.AccessDeniedException
import com.valr.order_book.exception.InsufficientFundsException
import com.valr.order_book.exception.InvalidOrderException
import com.valr.order_book.mapper.CurrencyPairMapper
import com.valr.order_book.mapper.TradeOrderMapper
import com.valr.order_book.model.*
import com.valr.order_book.repository.OrderRepository
import com.valr.order_book.repository.UserRepository
import com.valr.order_book.repository.UserWalletRepository
import com.valr.order_book.service.OrderQueue
import com.valr.order_book.service.OrderService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.stream.Collectors

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val userWalletRepository: UserWalletRepository,
    private val userRepository: UserRepository,
    private val orderQueue: OrderQueue
) : OrderService {

    /*
        A valid order request object must have the following
        - Price >=0
        - Quantity >=0
        - QuoteVolume >= 10
        This is a pseudo-method, validation of an order can include complex business rules
    */
    override fun validOrder(orderRequest: OrderRequestDto): Boolean {
        return orderRequest.price!! > BigDecimal.ZERO || orderRequest.quantity!! > BigDecimal.ZERO ||
                (orderRequest.price.multiply(orderRequest.quantity) >= BigDecimal("10"))
    }

    // This is a pseudo-method, this can be read from appropriate table of currencies or through an API
    override fun matchBuyCurrency(currency: CurrencyPairDto): Currency {
        return when (currency) {
            CurrencyPairDto.XRPZAR, CurrencyPairDto.BTCZAR -> Currency.ZAR
            else -> throw InvalidOrderException("Invalid currency pair")
        }
    }

    // This is a pseudo-method, this can be read from appropriate table of currencies or through an API
    override fun matchSellCurrency(currency: CurrencyPairDto): Currency {
        return when (currency) {
            CurrencyPairDto.XRPZAR -> Currency.XRP
            CurrencyPairDto.BTCZAR -> Currency.BTC
            else -> throw InvalidOrderException("Invalid currency pair.")
        }
    }

    /*
        Validate that user has sufficient funds to place an order
        I think here we first get user's wallets, then check the corresponding wallet to validate that the user has sufficient funds to place an order
        Also, I thought of a situation where a user has already placed an order for the currency pair, but it has not been executed
        E.g. User placed a SELL order of 100 XRPs, then places another SELL order of 20 XRPs while the previous order is still open
        In this scenario, I thought I should sum the open orders and the order the user is currently placing, if the total quantity is less than
        balance quantity in the wallet, user can proceed with placing the new order, else, throw InsufficientFundsException, but I did not cater for this scenario

        1. If user is placing a BUY order, we have to compute quote volume of current order, then ensure volume is not greater than
    */
    override fun fundsAvailable(userId: Long, orderRequest: OrderRequestDto): Boolean {
        val balance = userWalletRepository.walletBalance(
            userId,
            if (orderRequest.side == SideDto.BUY) matchBuyCurrency(orderRequest.pair!!)
            else matchSellCurrency(orderRequest.pair!!)
        )

        if (balance.isEmpty) {
            return false
        }

        return if (orderRequest.side == SideDto.BUY) {
            val volume = orderRequest.quantity?.multiply(orderRequest.price)
            balance.get().getQuantityDifference() >= volume
        } else
            balance.get().getQuantityDifference() >= orderRequest.quantity!!
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

        // Step 3: place an order
        val user = userRepository.findById(userId)
        if (user.isEmpty) {
            throw AccessDeniedException("Invalid user.")
        }

        val orderObj = TradeOrderMapper.INSTANCE.requestToInternal(user.get(), orderRequest)

        // Record this order in a DB
        val newOrder = orderRepository.save(orderObj)

        // Put new order in a queue for processing
        // My thinking is that, new orders should most likely be stored into a global queue
        // so that other running instances can access the queue for processing
        // I am also thinking each running instance most probably have multiple worker threads reading the queue
        // and since worker threads are running concurrently, they must access the queue in a thread safe way to ensure correct order processing
        orderQueue.addOrder(newOrder)

        // This return statement implies that an order was successfully placed, not that it was processed
        // It is the user's responsibility to check the status of the order
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