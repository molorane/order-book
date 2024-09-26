package com.valr.order_book.repository.projection

import com.valr.order_book.entity.enums.Currency
import java.math.BigDecimal

interface UserProjection {

    fun getId(): Long
    fun getFirstName(): String
    fun getWallets(): List<UserWalletProjection>

    interface UserWalletProjection {
        fun getId(): Long
        fun getCurrency(): Currency
        fun getQuantity(): BigDecimal
    }
}