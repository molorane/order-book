package com.valr.order_book.service

import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto

interface OrderService {
    fun validateOrder(orderRequest: OrderRequestDto): Boolean
    fun validateBalance(userId: String, orderRequest: OrderRequestDto): Boolean
    fun processOrder(userId: String, orderRequest: OrderRequestDto): OrderResponseDto
}