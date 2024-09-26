package com.valr.order_book.exception

data class ResourceNotFoundException(private val errorMessage: String) : RuntimeException(errorMessage) {
}