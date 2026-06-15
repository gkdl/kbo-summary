package com.kbo.summary.api.auth

import com.fasterxml.jackson.databind.JsonNode
import com.kbo.summary.core.exception.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

data class KakaoProfile(
    val kakaoId: String,
    val nickname: String,
)

/**
 * 카카오 access token 으로 사용자 정보를 조회한다.
 * GET https://kapi.kakao.com/v2/user/me  (Authorization: Bearer {accessToken})
 *
 * 응답에서 id(고유 회원번호)와 properties.nickname 을 추출한다.
 * 토큰이 유효하지 않으면 카카오가 401 을 반환하므로 인증 실패로 변환한다.
 */
@Component
class KakaoClient {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.builder()
        .baseUrl("https://kapi.kakao.com")
        .build()

    fun fetchProfile(accessToken: String): KakaoProfile {
        val body = runCatching {
            restClient.get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .retrieve()
                .body(JsonNode::class.java)
        }.getOrElse { e ->
            log.warn("카카오 사용자 조회 실패: {}", e.message)
            throw UnauthorizedException("카카오 인증에 실패했습니다")
        } ?: throw UnauthorizedException("카카오 응답이 비어 있습니다")

        val kakaoId = body.path("id").takeIf { !it.isMissingNode && !it.isNull }?.asText()
            ?: throw UnauthorizedException("카카오 사용자 ID를 확인할 수 없습니다")

        // 닉네임은 properties.nickname → kakao_account.profile.nickname 순으로 탐색
        val nickname = body.path("properties").path("nickname").asTextOrNull()
            ?: body.path("kakao_account").path("profile").path("nickname").asTextOrNull()
            ?: "야구팬${kakaoId.takeLast(4)}"

        return KakaoProfile(kakaoId = kakaoId, nickname = nickname)
    }

    private fun JsonNode.asTextOrNull(): String? =
        if (isMissingNode || isNull) null else asText().trim().takeIf { it.isNotEmpty() }
}
