package com.kbo.summary.core.dto

data class CreateReportRequest(
    val targetType: String,  // POST / COMMENT
    val targetId: Long,
    val reason: String,      // ABUSE / SPAM / SEXUAL / ETC
)
