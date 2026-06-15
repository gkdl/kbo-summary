package com.kbo.summary.api.auth

import com.kbo.summary.core.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder

/**
 * SecurityContext 에서 현재 로그인 회원의 memberId 를 꺼낸다.
 * 인증 정보가 없으면 UnauthorizedException(401).
 */
object CurrentMember {
    fun id(): Long = idOrNull() ?: throw UnauthorizedException("로그인이 필요합니다")

    /** 인증 정보가 없으면 null (조회 화면에서 '내 글 여부' 판단 등 비강제 용도) */
    fun idOrNull(): Long? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        return auth.principal as? Long
    }
}
