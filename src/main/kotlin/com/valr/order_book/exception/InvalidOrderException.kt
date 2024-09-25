package com.valr.order_book.exception

data class InvalidOrderException(private val errorMessage: String) : RuntimeException(errorMessage) {
}