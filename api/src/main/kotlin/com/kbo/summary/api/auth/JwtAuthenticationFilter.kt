package com.kbo.summary.api.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Authorization: Bearer {accessToken} 헤더를 검증해 SecurityContext 에 인증을 채운다.
 * 토큰이 없거나 invalid 면 인증 없이 통과시킨다(=익명). 보호는 SecurityConfig 의 경로 규칙이 담당.
 * principal 은 memberId(Long), authority 는 ROLE_{role} 로 설정한다.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)
        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            val claims = jwtProvider.parse(token)
            // access 토큰만 인증에 사용 (refresh 토큰으로는 API 호출 불가)
            if (claims != null && jwtProvider.tokenTypeOf(claims) == TokenType.ACCESS) {
                val memberId = jwtProvider.memberIdOf(claims)
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                val authentication = UsernamePasswordAuthenticationToken(memberId, null, authorities)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        return if (header.startsWith("Bearer ")) header.removePrefix("Bearer ").trim() else null
    }
}
