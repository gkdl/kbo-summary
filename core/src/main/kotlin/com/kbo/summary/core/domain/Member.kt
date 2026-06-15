package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import java.time.LocalDateTime

enum class MemberRole {
    USER,
    ADMIN,
}

enum class MemberStatus {
    ACTIVE,
    BANNED,
    WITHDRAWN,
}

@Entity
@Table(name = "TB_MEMBER")
class Member(
    @Column(name = "KAKAO_ID", length = 50, nullable = false, unique = true)
    val kakaoId: String,

    @Column(name = "NICKNAME", length = 30, nullable = false)
    var nickname: String,

    @Column(name = "TEAM_CODE", length = 10)
    var teamCode: String? = null,

    @Column(name = "ROLE", length = 10, nullable = false)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    var role: MemberRole = MemberRole.USER,

    @Column(name = "STATUS", length = 10, nullable = false)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    var status: MemberStatus = MemberStatus.ACTIVE,

    @Column(name = "CREATED_AT", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.SEQUENCE, generator = "member_seq")
    @SequenceGenerator(name = "member_seq", sequenceName = "SEQ_MEMBER", allocationSize = 1)
    @Column(name = "MEMBER_ID")
    val memberId: Long? = null,
)
