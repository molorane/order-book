package com.valr.order_book.repository

import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.OrderDto
import java.time.LocalDateTime


interface OrderRepository {
    fun tradeHistory(
        currencyPair: CurrencyPairDto,
        skip: Int,
        limit: Int,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): List<OrderDto>
}