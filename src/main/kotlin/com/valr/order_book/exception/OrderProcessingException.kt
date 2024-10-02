package com.valr.order_book.exception

data class OrderProcessingException(private val errorMessage: String) : RuntimeException(errorMessage) {
}