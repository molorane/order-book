package com.valr.order_book.api

import com.valr.order_book.controller.OrderApiDelegate
import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.OrderBookDto
import com.valr.order_book.model.OrderRequestDto
import com.valr.order_book.model.OrderResponseDto
import com.valr.order_book.service.OrderService
import lombok.AllArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
@AllArgsConstructor
class OrderController(private val orderService: OrderService) : OrderApiDelegate {

    override fun placeOrder(orderRequestDto: OrderRequestDto): ResponseEntity<OrderResponseDto> {
        return ResponseEntity.ok(orderService.processOrder(2024, orderRequestDto))
    }

    override fun orderBook(currencyPair: CurrencyPairDto): ResponseEntity<OrderBookDto> {
        return ResponseEntity.ok(orderService.orderBook(currencyPair))
    }
}