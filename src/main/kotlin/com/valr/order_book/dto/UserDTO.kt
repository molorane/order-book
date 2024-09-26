package com.valr.order_book.dto

import com.valr.order_book.entity.enums.Currency
import java.math.BigDecimal

data class UserDTO(
    val id: Long,
    val firstName: String,
    val wallets: List<UserWalletDTO>
)

data class UserWalletDTO(
    val id: Long,
    val currency: Currency,
    val quantity: BigDecimal
)