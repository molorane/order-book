package com.valr.order_book.service

import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.TradeDto
import java.time.LocalDateTime

interface TradeService {
    fun tradeHistory(
        currencyPair: CurrencyPairDto,
        skip: Int? = 0,
        limit: Int? = 1,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null
    ): List<TradeDto>
}