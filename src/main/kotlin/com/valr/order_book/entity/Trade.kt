package com.valr.order_book.entity

import com.valr.order_book.entity.enums.*
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
data class Trade(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_order_id", nullable = false)
    var seller: TradeOrder? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_order_id", nullable = false)
    var buyer: TradeOrder? = null,

    val quantity: BigDecimal = 0.toBigDecimal(),

    val price: BigDecimal = 0.toBigDecimal(),

    val tradedAt: LocalDateTime = LocalDateTime.now(),
)