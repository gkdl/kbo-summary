package com.kbo.summary.core.dto

data class KakaoLoginRequest(
    val kakaoAccessToken: String,
)

data class RefreshRequest(
    val refreshToken: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val member: MemberDto,
)

data class MemberDto(
    val memberId: Long,
    val nickname: String,
    val teamCode: String?,
    val role: String,
)
