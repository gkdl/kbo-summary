package com.kbo.summary.api.service

import com.kbo.summary.api.auth.JwtProvider
import com.kbo.summary.api.auth.KakaoClient
import com.kbo.summary.api.auth.TokenType
import com.kbo.summary.api.repository.MemberRepository
import com.kbo.summary.core.domain.Member
import com.kbo.summary.core.domain.MemberStatus
import com.kbo.summary.core.dto.MemberDto
import com.kbo.summary.core.dto.TokenResponse
import com.kbo.summary.core.exception.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuthService(
    private val kakaoClient: KakaoClient,
    private val jwtProvider: JwtProvider,
    private val memberRepository: MemberRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /** 카카오 access token 으로 로그인. 신규면 회원 생성, 기존이면 닉네임 동기화 후 우리 JWT 발급. */
    @Transactional
    fun kakaoLogin(kakaoAccessToken: String): TokenResponse {
        val profile = kakaoClient.fetchProfile(kakaoAccessToken)

        // 탈퇴 회원은 kakaoId 를 파기하므로 findByKakaoId 로 잡히지 않는다 →
        // 같은 카카오로 재로그인하면 항상 '새 회원'으로 시작한다(탈퇴는 영구).
        val member = memberRepository.findByKakaoId(profile.kakaoId)?.also { existing ->
            if (existing.status == MemberStatus.BANNED) {
                throw UnauthorizedException("이용이 제한된 계정입니다")
            }
            existing.nickname = profile.nickname
        } ?: memberRepository.save(
            Member(kakaoId = profile.kakaoId, nickname = profile.nickname),
        )

        log.info("카카오 로그인: memberId={} nickname={}", member.memberId, member.nickname)
        return issueFor(member)
    }

    /** refresh 토큰으로 access/refresh 재발급 */
    @Transactional(readOnly = true)
    fun refresh(refreshToken: String): TokenResponse {
        val claims = jwtProvider.parse(refreshToken)
            ?: throw UnauthorizedException("유효하지 않은 토큰입니다")
        if (jwtProvider.tokenTypeOf(claims) != TokenType.REFRESH) {
            throw UnauthorizedException("refresh 토큰이 아닙니다")
        }
        val member = memberRepository.findByIdOrNull(jwtProvider.memberIdOf(claims))
            ?: throw UnauthorizedException("존재하지 않는 회원입니다")
        if (member.status != MemberStatus.ACTIVE) {
            throw UnauthorizedException("이용이 제한된 계정입니다")
        }
        return issueFor(member)
    }

    @Transactional(readOnly = true)
    fun me(memberId: Long): MemberDto =
        (memberRepository.findByIdOrNull(memberId)
            ?: throw UnauthorizedException("존재하지 않는 회원입니다")).toDto()

    /**
     * 회원 탈퇴 — 카카오 연결 식별자(kakaoId)를 복구 불가능한 토큰으로 파기한다.
     * - kakaoId 를 덮어써 카카오 계정과의 연결을 끊는다(원본 식별자 복구 불가).
     * - 작성한 글·댓글은 '탈퇴한 회원'으로 익명 보존(FK 무결성 위해 회원 행 자체는 유지).
     * - 같은 카카오로 다시 로그인하면 새 회원으로 시작(탈퇴는 영구).
     * 앱 쪽에서는 탈퇴 시 카카오 unlink 까지 호출해 양쪽 연결을 모두 해제한다.
     */
    @Transactional
    fun withdraw(memberId: Long) {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw UnauthorizedException("존재하지 않는 회원입니다")
        member.kakaoId = "withdrawn:${UUID.randomUUID()}"
        member.status = MemberStatus.WITHDRAWN
        member.nickname = "탈퇴한 회원"
        member.teamCode = null
        log.info("회원 탈퇴(식별자 파기): memberId={}", memberId)
    }

    private fun issueFor(member: Member): TokenResponse {
        val tokens = jwtProvider.issue(member.memberId!!)
        return TokenResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            member = member.toDto(),
        )
    }

    private fun Member.toDto() = MemberDto(
        memberId = memberId!!,
        nickname = nickname,
        teamCode = teamCode,
        role = role.name,
    )
}
