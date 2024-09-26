package com.valr.order_book.service.impl

import com.valr.order_book.entity.TradeOrder
import com.valr.order_book.entity.User
import com.valr.order_book.entity.UserWallet
import com.valr.order_book.entity.enums.TakerSide
import com.valr.order_book.exception.InsufficientFundsException
import com.valr.order_book.exception.InvalidOrderException
import com.valr.order_book.mapper.CurrencyPairMapper
import com.valr.order_book.mapper.TradeMapper
import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto
import com.valr.order_book.model.SideDto
import com.valr.order_book.repository.TradeRepository
import com.valr.order_book.repository.UserRepository
import com.valr.order_book.repository.projection.UserProjection
import com.valr.order_book.service.OrderService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*


@Service
class OrderServiceImpl(private val tradeRepository: TradeRepository, private val userRepository: UserRepository) : OrderService {

    // Sell orders (min-heap for lowest price)
    private val sellOrders: PriorityQueue<TradeOrder> =
        PriorityQueue<TradeOrder>(java.util.Comparator<TradeOrder> { a: TradeOrder, b: TradeOrder ->
            a.price.compareTo(b.price)
        })

    // Buy orders (max-heap for highest price)
    private val buyOrders: PriorityQueue<TradeOrder> =
        PriorityQueue<TradeOrder>(java.util.Comparator<TradeOrder> { a: TradeOrder, b: TradeOrder ->
            b.price.compareTo(a.price)
        })

    /*
        A valid order request object must have
        Price <=0 and Quality <=0
        QuoteVolume >= 10
    */
    override fun validOrder(orderRequest: OrderRequestDto): Boolean {
        return orderRequest.price!! > BigDecimal.ZERO || orderRequest.quantity!! > BigDecimal.ZERO ||
                (orderRequest.price.multiply(orderRequest.quantity) >= BigDecimal("10"))
    }

    /*
        Validate that user has sufficient funds to place an order
        I think here we first get user's wallets, then check the corresponding wallet
        to validate that the user has sufficient funds to make a trade
    */
    override fun fundsAvailable(userId: Long, orderRequest: OrderRequestDto): Boolean {
        val user = userRepository.findUserWithWallets(userId);
        if (!user.isPresent) {
            return false
        }

        println(user)

//        val currency = orderRequest.pair?.let { CurrencyPairMapper.INSTANCE.dtoToInternal(it) }
//        val wallets: List<UserProjection.Wallet> = user.get().getWallets()
//
//        if(orderRequest.side == SideDto.BUY) {
//            val matchingWallet = wallets.filter { it.getCurrency().value == currency?.value?.substring(2) }
//
//            if(matchingWallet.isEmpty()) {
//                return false
//            }
//
//            val volume = orderRequest.quantity?.multiply(orderRequest.price)
//            if(matchingWallet.first().getQuantity() < volume) {
//                return false
//            }
//        }
//
//        if(orderRequest.side == SideDto.SELL) {
//            val matchingWallet = wallets.filter { it.getCurrency().value == currency?.value?.substring(0, 2) }
//
//            if(matchingWallet.isEmpty()) {
//                return false
//            }
//
//            if(matchingWallet.first().getQuantity() < orderRequest.quantity) {
//                return false
//            }
//        }

        return true;
    }

    override fun processOrder(userId: Long, orderRequest: OrderRequestDto): OrderResponseDto {

        // Step 1: Ensure the order request object is valid
        if (!validOrder(orderRequest)) {
            throw InvalidOrderException("Invalid order request.")
        }

        // Step 2: Ensure the user has sufficient funds to place an order
        if (!fundsAvailable(userId, orderRequest)) {
            throw InsufficientFundsException("Insufficient balance.")
        }

        // Step 3: process the order
        val orderObj = TradeMapper.INSTANCE.requestToInternal(orderRequest)

        // Record this order in a DB
        val placeOder = tradeRepository.save(orderObj)

        // Put new order in a queue for processing
        // My thinking is that, placeOder should most likely be sent to Cache(Redis, Memcached etc) for global access
        // so that other instances can access it and perhaps process it
        if(orderObj.takerSide == TakerSide.BUY) {
            buyOrders.add(placeOder)
        } else {
            sellOrders.add(placeOder)
        }

        return TradeMapper.INSTANCE.internalToOrderResponse(placeOder)
    }
}