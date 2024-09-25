package com.valr.order_book.exception

data class InsufficientFundsException(private val errorMessage: String) : RuntimeException(errorMessage) {
}