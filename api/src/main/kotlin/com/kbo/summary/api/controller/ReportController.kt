package com.kbo.summary.api.controller

import com.kbo.summary.api.auth.CurrentMember
import com.kbo.summary.api.service.ReportService
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.dto.CreateReportRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ReportController(
    private val reportService: ReportService,
) {
    @PostMapping("/api/reports")
    fun report(@RequestBody request: CreateReportRequest): ApiResponse<Unit> {
        reportService.report(CurrentMember.id(), request)
        return ApiResponse.ok(Unit)
    }
}
