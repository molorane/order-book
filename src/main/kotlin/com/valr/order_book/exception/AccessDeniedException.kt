package com.valr.order_book.exception

data class AccessDeniedException(private val errorMessage: String) : RuntimeException(errorMessage) {
}