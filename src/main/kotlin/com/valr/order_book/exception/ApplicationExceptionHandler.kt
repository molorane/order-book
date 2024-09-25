package com.valr.order_book.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class ApplicationExceptionHandler {

    @ResponseBody
    @ExceptionHandler(DataNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun dataNotFoundHandler(ex: DataNotFoundException, request: HttpServletRequest): ApiError {
        return ApiError(ex.message, request.pathInfo, LocalDateTime.now())
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun noContentHandler(ex: AccessDeniedException, request: HttpServletRequest): ApiError {
        return ApiError(ex.message, request.pathInfo, LocalDateTime.now())
    }
}