package com.kbo.summary.api.controller

import com.kbo.summary.api.auth.CurrentMember
import com.kbo.summary.api.service.AuthService
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.dto.KakaoLoginRequest
import com.kbo.summary.core.dto.MemberDto
import com.kbo.summary.core.dto.RefreshRequest
import com.kbo.summary.core.dto.TokenResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/api/auth/kakao")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(authService.kakaoLogin(request.kakaoAccessToken))

    @PostMapping("/api/auth/refresh")
    fun refresh(@RequestBody request: RefreshRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(authService.refresh(request.refreshToken))

    @GetMapping("/api/members/me")
    fun me(): ApiResponse<MemberDto> =
        ApiResponse.ok(authService.me(CurrentMember.id()))

    @DeleteMapping("/api/members/me")
    fun withdraw(): ApiResponse<Unit> {
        authService.withdraw(CurrentMember.id())
        return ApiResponse.ok(Unit)
    }
}
