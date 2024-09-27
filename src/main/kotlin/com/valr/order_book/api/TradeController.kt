package com.valr.order_book.api

import com.valr.order_book.controller.TradeApiDelegate
import com.valr.order_book.model.CurrencyPairDto
import com.valr.order_book.model.TradeDto
import com.valr.order_book.service.TradeService
import lombok.AllArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@AllArgsConstructor
class TradeController(
    private val tradeService: TradeService
) : TradeApiDelegate {

    override fun tradeHistory(
        currencyPair: CurrencyPairDto,
        skip: Int,
        limit: Int,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): ResponseEntity<List<TradeDto>> {
        return ResponseEntity.ok(tradeService.tradeHistory(currencyPair, skip, limit, startTime, endTime))
    }
}