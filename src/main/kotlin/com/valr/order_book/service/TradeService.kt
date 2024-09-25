package com.valr.order_book.service

import com.valr.order_book.model.*
import java.time.LocalDateTime

interface TradeService {
    fun tradeHistory(
        currencyPair: CurrencyPairDto,
        skip: Int? = 0,
        limit: Int? = 1,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null
    ): List<TradeOrderDto>

    fun orderBook(currencyPair: CurrencyPairDto): OrderBookDto
    fun placeLimitOrder(orderRequestDto: OrderRequestDto): OrderResponseDto
}