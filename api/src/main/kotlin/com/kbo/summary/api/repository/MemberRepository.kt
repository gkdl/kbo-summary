package com.kbo.summary.api.repository

import com.kbo.summary.core.domain.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByKakaoId(kakaoId: String): Member?
}
