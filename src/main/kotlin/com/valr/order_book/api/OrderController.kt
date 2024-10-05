package com.valr.order_book.api

import com.valr.order_book.controller.OrderApiDelegate
import com.valr.order_book.exception.AccessDeniedException
import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.OrderBookDto
import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto
import com.valr.order_book.service.OrderService
import jakarta.servlet.http.HttpServletRequest
import lombok.AllArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
@AllArgsConstructor
class OrderController(
    private val orderService: OrderService,
    private val httpServletRequest: HttpServletRequest
) : OrderApiDelegate {

    override fun placeOrder(orderRequestDto: OrderRequestDto): ResponseEntity<OrderResponseDto> {
        val userIdHeader: String = httpServletRequest.getHeader("X-VALR-USER-ID")
            ?: throw AccessDeniedException("User ID is missing")

        val userId = userIdHeader.toLong()

        return ResponseEntity.ok(orderService.placeOrder(userId, orderRequestDto))
    }

    override fun orderBook(currencyPair: CurrencyPairDto): ResponseEntity<OrderBookDto> {
        return ResponseEntity.ok(orderService.orderBook(currencyPair))
    }
}