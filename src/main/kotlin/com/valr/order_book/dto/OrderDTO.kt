package com.valr.order_book.dto

import com.valr.order_book.entity.enums.CurrencyPair
import com.valr.order_book.entity.enums.TakerSide
import java.math.BigDecimal

data class OrderDTO(
    val side: TakerSide,

    val quantity: BigDecimal = 0.toBigDecimal(),

    val price: BigDecimal = 0.toBigDecimal(),

    val currencyPair: CurrencyPair,

    val orderCount: Long,
)