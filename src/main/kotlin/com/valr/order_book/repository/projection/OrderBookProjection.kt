package com.valr.order_book.repository.projection

import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.TakerSide
import java.math.BigDecimal

interface OrderBookProjection {

    fun getTakerSide(): TakerSide
    fun getQuantity(): BigDecimal
    fun getPrice(): BigDecimal
    fun getCurrencyPair(): CurrencyPair
    fun getOrderCount(): Int
}