package com.valr.order_book.exception

import java.time.LocalDateTime


data class ApiError(
    val message: String?,
    val path: String,
    val dateTime: LocalDateTime
)