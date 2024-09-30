package com.valr.order_book.repository.projection

import java.math.BigDecimal

interface WalletBalance {

    fun getTotalIn(): BigDecimal
    fun getTotalOut(): BigDecimal
    fun getQuantityDifference(): BigDecimal
}