package com.kbo.summary.core.exception

enum class ErrorCode(val httpStatus: Int, val defaultMessage: String) {
    INTERNAL_ERROR(500, "내부 서버 오류가 발생했습니다."),
    INVALID_INPUT(400, "잘못된 요청입니다."),
    RESOURCE_NOT_FOUND(404, "요청한 리소스를 찾을 수 없습니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    CRAWLING_FAILED(502, "데이터 수집에 실패했습니다."),
    EXTERNAL_API_ERROR(502, "외부 API 호출에 실패했습니다."),
    PARSE_FAILED(502, "데이터 파싱에 실패했습니다."),
    SUMMARY_FAILED(500, "경기 요약 생성에 실패했습니다."),
}

abstract class BusinessException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.defaultMessage,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class ResourceNotFoundException(
    message: String = ErrorCode.RESOURCE_NOT_FOUND.defaultMessage,
    cause: Throwable? = null,
) : BusinessException(ErrorCode.RESOURCE_NOT_FOUND, message, cause)

class InvalidInputException(
    message: String = ErrorCode.INVALID_INPUT.defaultMessage,
    cause: Throwable? = null,
) : BusinessException(ErrorCode.INVALID_INPUT, message, cause)

class UnauthorizedException(
    message: String = ErrorCode.UNAUTHORIZED.defaultMessage,
    cause: Throwable? = null,
) : BusinessException(ErrorCode.UNAUTHORIZED, message, cause)

class CrawlingException(
    message: String = ErrorCode.CRAWLING_FAILED.defaultMessage,
    cause: Throwable? = null,
) : BusinessException(ErrorCode.CRAWLING_FAILED, message, cause)

class ExternalApiException(
    message: String = ErrorCode.EXTERNAL_API_ERROR.defaultMessage,
    cause: Throwable? = null,
) : BusinessException(ErrorCode.EXTERNAL_API_ERROR, message, cause)

data class ErrorResponse(
    val code: String,
    val message: String,
    val status: Int,
) {
    companion object {
        fun of(errorCode: ErrorCode, message: String = errorCode.defaultMessage): ErrorResponse =
            ErrorResponse(code = errorCode.name, message = message, status = errorCode.httpStatus)

        fun of(exception: BusinessException): ErrorResponse =
            of(exception.errorCode, exception.message)
    }
}
