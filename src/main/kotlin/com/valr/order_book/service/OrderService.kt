package com.valr.order_book.service

import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto

interface OrderService {
    fun validateOrder(orderRequest: OrderRequestDto): Boolean
    fun checkUserBalance(userId: String): Boolean
    fun processOrder(orderRequest: OrderRequestDto): OrderResponseDto
}