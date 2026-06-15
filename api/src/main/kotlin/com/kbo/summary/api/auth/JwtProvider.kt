package com.kbo.summary.api.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

enum class TokenType { ACCESS, REFRESH }

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)

/**
 * JWT 발급·검증. access 토큰은 짧게(1시간), refresh 는 길게(30일).
 * subject = memberId, claim "type" 으로 access/refresh 를 구분한다.
 */
@Component
class JwtProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-token-validity-seconds}") private val accessValiditySeconds: Long,
    @Value("\${jwt.refresh-token-validity-seconds}") private val refreshValiditySeconds: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun issue(memberId: Long): TokenPair = TokenPair(
        accessToken = build(memberId, TokenType.ACCESS, accessValiditySeconds),
        refreshToken = build(memberId, TokenType.REFRESH, refreshValiditySeconds),
    )

    private fun build(memberId: Long, type: TokenType, validitySeconds: Long): String {
        val now = Date()
        val expiry = Date(now.time + validitySeconds * 1000)
        return Jwts.builder()
            .subject(memberId.toString())
            .claim("type", type.name)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    /** 유효하면 claims 반환, 만료·위조면 null */
    fun parse(token: String): Claims? = runCatching {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
    }.getOrNull()

    fun memberIdOf(claims: Claims): Long = claims.subject.toLong()

    fun tokenTypeOf(claims: Claims): TokenType? =
        runCatching { TokenType.valueOf(claims["type"] as String) }.getOrNull()
}
