package com.valr.order_book.exception

import java.time.LocalDateTime


data class ApiError(
    private val _message: String?,
    val path: String,
    val dateTime: LocalDateTime
) {
    val message: String
        get() = _message ?: "Encountered an unexpected error."
}