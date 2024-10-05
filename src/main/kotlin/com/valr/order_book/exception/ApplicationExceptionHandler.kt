package com.valr.order_book.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.LocalDateTime

@ControllerAdvice
class ApplicationExceptionHandler {

    @ResponseBody
    @ExceptionHandler(ResourceNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun resourceNotFoundException(ex: ResourceNotFoundException, request: HttpServletRequest): ApiError {
        return ApiError(ex.message, request.requestURI, LocalDateTime.now())
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun noContentHandler(ex: AccessDeniedException, request: HttpServletRequest): ApiError {
        return ApiError(ex.message, request.requestURI, LocalDateTime.now())
    }

    @ResponseBody
    @ExceptionHandler(InsufficientFundsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun insufficientFundsException(ex: InsufficientFundsException, request: HttpServletRequest): ApiError {
        return ApiError(ex.message, request.requestURI, LocalDateTime.now())
    }

    @ResponseBody
    @ExceptionHandler(InvalidOrderException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidOrderException(ex: InvalidOrderException, request: HttpServletRequest): ApiError {
        return ApiError(ex.message, request.requestURI, LocalDateTime.now())
    }

    @ResponseBody
    @ExceptionHandler(OrderProcessingException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun orderProcessingException(ex: OrderProcessingException, request: HttpServletRequest): ApiError {
        return ApiError(ex.message, request.requestURI, LocalDateTime.now())
    }
}