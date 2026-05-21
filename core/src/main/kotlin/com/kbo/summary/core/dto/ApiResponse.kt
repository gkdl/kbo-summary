package com.kbo.summary.core.dto

import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val cachedAt: LocalDateTime? = null,
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> =
            ApiResponse(success = true, data = data, cachedAt = LocalDateTime.now())

        fun <T> fail(error: ApiError): ApiResponse<T> =
            ApiResponse(success = false, error = error)
    }
}

data class ApiError(
    val code: String,
    val message: String,
)
