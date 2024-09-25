package com.valr.order_book.service.impl

import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto
import com.valr.order_book.service.OrderService
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class OrderServiceImpl : OrderService {

    override fun validateOrder(orderRequest: OrderRequestDto): Boolean {
        return orderRequest.price!! <= BigDecimal.ZERO || orderRequest.quantity!! <= BigDecimal.ZERO
    }

    override fun checkUserBalance(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun processOrder(orderRequest: OrderRequestDto): OrderResponseDto {
        TODO("Not yet implemented")
    }
}