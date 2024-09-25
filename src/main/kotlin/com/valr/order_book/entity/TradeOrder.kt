package com.valr.order_book.entity

import com.valr.order_book.entity.enums.*
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
data class TradeOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val sequenceId: Long? = null,

    var id: String? = null,

    @Enumerated(EnumType.STRING)
    val takerSide: TakerSide = TakerSide.SELL,

    val quantity: BigDecimal = 0.toBigDecimal(),

    val price: BigDecimal = 0.toBigDecimal(),

    val quoteVolume: BigDecimal = 0.toBigDecimal(),

    @Enumerated(EnumType.STRING)
    val currencyPair: CurrencyPair = CurrencyPair.BTCZAR,

    @Enumerated(EnumType.STRING)
    val status: Status = Status.PLACED,

    val tradedAt: LocalDateTime = LocalDateTime.now(),

    val postOnly: Boolean = false,

    val customerOrderId: String? = null,

    @Enumerated(EnumType.STRING)
    val timeInForce: TimeInForce = TimeInForce.GTC,

    @Enumerated(EnumType.STRING)
    val orderType: OrderType = OrderType.LIMIT_ORDER,

    val allowMargin: Boolean = false,

    val reduceOnly: Boolean = false,
)