package com.kbo.summary.core.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)

        fun <T> fail(error: ApiError): ApiResponse<T> = ApiResponse(success = false, error = error)
    }
}

data class ApiError(
    val code: String,
    val message: String,
)
