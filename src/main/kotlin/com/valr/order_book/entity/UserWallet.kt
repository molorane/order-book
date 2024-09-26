package com.valr.order_book.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.valr.order_book.entity.enums.Currency
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
data class UserWallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    val currency: Currency = Currency.ZAR,

    val quantity: BigDecimal = 0.toBigDecimal(),

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private var user: User
)