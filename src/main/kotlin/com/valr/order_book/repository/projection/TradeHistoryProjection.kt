package com.valr.order_book.repository.projection

import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.TakerSide
import org.springframework.beans.factory.annotation.Value
import java.math.BigDecimal
import java.time.LocalDateTime

interface TradeHistoryProjection {

    fun getPrice(): BigDecimal
    fun getQuantity(): BigDecimal
    fun getCurrencyPair(): CurrencyPair
    fun getTakerSide(): TakerSide
    fun getSequenceId(): Long
    fun getId(): String
    fun getTradedAt(): LocalDateTime

    @Value("#{target.price * target.quantity}")
    fun getQuoteVolume(): BigDecimal
}