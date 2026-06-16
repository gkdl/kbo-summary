package com.kbo.summary.api.repository

import com.kbo.summary.core.domain.Report
import com.kbo.summary.core.domain.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, Long> {
    fun existsByTargetTypeAndTargetIdAndReporterId(
        targetType: ReportTargetType,
        targetId: Long,
        reporterId: Long,
    ): Boolean

    fun countByTargetTypeAndTargetId(targetType: ReportTargetType, targetId: Long): Int
}
