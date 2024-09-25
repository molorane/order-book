package com.valr.order_book.exception

data class DataNotFoundException(private val errorMessage: String) : RuntimeException(errorMessage) {
}