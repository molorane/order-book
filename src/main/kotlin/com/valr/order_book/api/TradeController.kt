package com.valr.order_book.api

import com.valr.order_book.controller.TradeApiDelegate
import com.valr.order_book.model.*
import com.valr.order_book.repository.OrderRepository
import com.valr.order_book.service.TradeService
import lombok.AllArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@AllArgsConstructor
class TradeController(
    private val tradeService: TradeService,
    private val orderRepository: OrderRepository
    ) : TradeApiDelegate {

    override fun tradeHistory(
        currencyPair: CurrencyPairDto,
        skip: Int,
        limit: Int,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): ResponseEntity<List<TradeOrderDto>> {
        val list = orderRepository.tradeHistory(currencyPair, skip, limit, startTime, endTime)
        return ResponseEntity.ok(tradeService.tradeHistory(currencyPair, skip, limit, startTime, endTime))
    }

    override fun orderBook(currencyPair: CurrencyPairDto): ResponseEntity<OrderBookDto> {
        return ResponseEntity.ok(tradeService.orderBook(currencyPair))
    }
}