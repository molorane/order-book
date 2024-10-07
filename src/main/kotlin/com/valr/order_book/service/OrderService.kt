package com.valr.order_book.service

import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.OrderBookDto
import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto

interface OrderService {
    fun validOrder(orderRequest: OrderRequestDto): Boolean
    fun fundsAvailable(userId: Long, orderRequest: OrderRequestDto): Boolean
    fun placeOrder(userId: Long, orderRequest: OrderRequestDto): OrderResponseDto
    fun orderBook(currencyPair: CurrencyPairDto): OrderBookDto
}