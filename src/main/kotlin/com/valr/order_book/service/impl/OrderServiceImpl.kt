package com.valr.order_book.service.impl

import com.valr.order_book.exception.InvalidOrderException
import com.valr.order_book.mapper.TradeMapper
import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto
import com.valr.order_book.repository.TradeRepository
import com.valr.order_book.service.OrderService
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class OrderServiceImpl(private val tradeRepository: TradeRepository) : OrderService {

    /*
        A valid order request object must have
        Price <=0 and Quality <=0
        QuoteVolume >= 10
    */
    override fun validateOrder(orderRequest: OrderRequestDto): Boolean {
        return orderRequest.price!! <= BigDecimal.ZERO || orderRequest.quantity!! <= BigDecimal.ZERO ||
                (orderRequest.price * orderRequest.quantity <= BigDecimal("10"))
    }

    /*
        Validate that user has sufficient funds to place an order
        I think here we first get user's wallets, then check the corresponding wallet
        E.g, if user wants to purchase XRPs using FIAT(ZAR), we have to check whether they have enough
        ZAR to make a purchase
    */
    override fun validateBalance(userId: String, orderRequest: OrderRequestDto): Boolean {
        TODO("Not yet implemented")
    }

    override fun processOrder(userId: String, orderRequest: OrderRequestDto): OrderResponseDto {

        // Step 1: Ensure the order request object is valid
        if (!validateOrder(orderRequest)) {
            throw InvalidOrderException("Invalid order request.")
        }

        // Step 2: Ensure the user has sufficient funds to place an order
        if (!validateBalance(userId, orderRequest)) {
            throw InvalidOrderException("Insufficient balance.")
        }

        // Step 3: process the order
        // First record this order first in a DB
        // Second in a queue for processing
        val orderObj = TradeMapper.INSTANCE.requestToInternal(orderRequest)
        val placeOder = tradeRepository.save(orderObj)

        return TradeMapper.INSTANCE.internalToOrderResponse(placeOder)
    }
}