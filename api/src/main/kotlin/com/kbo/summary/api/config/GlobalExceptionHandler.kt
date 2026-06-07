package com.kbo.summary.api.config

import com.kbo.summary.core.dto.ApiError
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.exception.BusinessException
import com.kbo.summary.core.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

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

    // 매핑되지 않은 경로(루트 GET, favicon, 봇 스캔, 잘못된 모바일 URL 등)는 ERROR 가 아니라 404 + INFO 로깅
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNotFound(
        e: NoResourceFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.info("404 매핑 없음: {} {}", request.method, request.requestURI)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.fail(ApiError("NOT_FOUND", "요청한 리소스를 찾을 수 없습니다")))
    }

    // 그 외 예측 못한 예외 — 디버깅 위해 method/URI/query 를 같이 찍는다
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val query = request.queryString?.let { "?$it" } ?: ""
        log.error("처리되지 않은 예외 — {} {}{}", request.method, request.requestURI, query, e)
        val errorCode = ErrorCode.INTERNAL_ERROR
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(ApiResponse.fail(ApiError(errorCode.name, errorCode.defaultMessage)))
    }
}
