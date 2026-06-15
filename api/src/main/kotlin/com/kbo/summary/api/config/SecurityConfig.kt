package com.kbo.summary.api.config

import com.kbo.summary.api.auth.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    // 기존 조회 API(경기·선수·팀·순위 등)는 전부 공개. 로그인/회원 관련만 인증을 요구한다.
    // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 배치해 Bearer 토큰을 먼저 처리.
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            cors { }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                // 인증이 필요한 경로 — 내 정보 조회/탈퇴
                authorize(HttpMethod.GET, "/api/members/me", authenticated)
                authorize(HttpMethod.DELETE, "/api/members/me", authenticated)
                // 게시글 작성·삭제·좋아요, 댓글 작성·삭제는 로그인 필요 (조회는 공개)
                authorize(HttpMethod.POST, "/api/posts", authenticated)
                authorize(HttpMethod.DELETE, "/api/posts/*", authenticated)
                authorize(HttpMethod.POST, "/api/posts/*/like", authenticated)
                authorize(HttpMethod.POST, "/api/posts/*/comments", authenticated)
                authorize(HttpMethod.DELETE, "/api/comments/*", authenticated)
                // 그 외(로그인·조회 API·actuator·swagger 등)는 공개
                authorize(anyRequest, permitAll)
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
