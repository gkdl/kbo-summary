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

        val member = memberRepository.findByKakaoId(profile.kakaoId)?.also { existing ->
            if (existing.status == MemberStatus.BANNED) {
                throw UnauthorizedException("이용이 제한된 계정입니다")
            }
            // 탈퇴 후 재로그인 시 계정 재활성화
            if (existing.status == MemberStatus.WITHDRAWN) existing.status = MemberStatus.ACTIVE
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

    /** 회원 탈퇴 — 상태만 WITHDRAWN 으로 (작성 글 보존). 카카오 닉네임은 익명화. */
    @Transactional
    fun withdraw(memberId: Long) {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw UnauthorizedException("존재하지 않는 회원입니다")
        member.status = MemberStatus.WITHDRAWN
        member.nickname = "탈퇴한 회원"
        log.info("회원 탈퇴: memberId={}", memberId)
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
