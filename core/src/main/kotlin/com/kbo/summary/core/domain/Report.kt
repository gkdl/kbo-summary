package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import java.time.LocalDateTime

enum class ReportTargetType {
    POST,
    COMMENT,
}

enum class ReportReason {
    ABUSE,    // 욕설/혐오
    SPAM,     // 스팸/광고
    SEXUAL,   // 음란물
    ETC,      // 기타
}

@Entity
@Table(name = "TB_REPORT")
class Report(
    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_TYPE", length = 10, nullable = false)
    val targetType: ReportTargetType,

    @Column(name = "TARGET_ID", nullable = false)
    val targetId: Long,

    @Column(name = "REPORTER_ID", nullable = false)
    val reporterId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "REASON", length = 20, nullable = false)
    val reason: ReportReason,

    @Column(name = "CREATED_AT", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq")
    @SequenceGenerator(name = "report_seq", sequenceName = "SEQ_REPORT", allocationSize = 1)
    @Column(name = "REPORT_ID")
    val reportId: Long? = null,
)
