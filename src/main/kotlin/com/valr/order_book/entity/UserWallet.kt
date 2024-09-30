package com.valr.order_book.entity

import com.valr.order_book.entity.enums.Currency
import com.valr.order_book.entity.enums.FlowType
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
data class UserWallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    val currency: Currency = Currency.ZAR,

    @Enumerated(EnumType.STRING)
    val flowType: FlowType = FlowType.IN,

    val quantity: BigDecimal = 0.toBigDecimal(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private var user: User
)