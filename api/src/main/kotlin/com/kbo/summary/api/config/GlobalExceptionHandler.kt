package com.kbo.summary.api.config

import com.kbo.summary.core.dto.ApiError
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.exception.BusinessException
import com.kbo.summary.core.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    // BusinessException 계층(GameNotFound/SearchEmpty/Summary 등)은 ErrorCode의 HTTP 상태로 매핑한다
    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("비즈니스 예외 [{}] {}", e.errorCode.name, e.message)
        return ResponseEntity
            .status(e.errorCode.httpStatus)
            .body(ApiResponse.fail(ApiError(e.errorCode.name, e.message)))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("처리되지 않은 예외", e)
        val errorCode = ErrorCode.INTERNAL_ERROR
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(ApiResponse.fail(ApiError(errorCode.name, errorCode.defaultMessage)))
    }
}
