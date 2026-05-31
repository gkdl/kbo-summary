package com.kbo.summary.api.controller

import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.crawler.service.BulkCrawlResult
import com.kbo.summary.crawler.service.BulkCrawlService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/admin/crawl")
class AdminCrawlController(
    private val bulkCrawlService: BulkCrawlService,
) {
    /**
     * 지정 날짜 범위의 경기 일정 + 스코어 전체 수집.
     *
     * POST /api/admin/crawl/games?from=20260322&to=20260531
     */
    @PostMapping("/games")
    fun crawlGames(
        @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") from: LocalDate,
        @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") to: LocalDate,
    ): ApiResponse<BulkCrawlResult> {
        require(!from.isAfter(to)) { "from 은 to 보다 앞이어야 합니다" }
        require(!from.isBefore(LocalDate.of(2000, 1, 1))) { "from 이 너무 오래됐습니다" }
        return ApiResponse.ok(bulkCrawlService.crawlGamesInRange(from, to))
    }

    /**
     * 전 팀 로스터 + 타자/투수/팀 시즌 기록 + 순위 한 번에 수집.
     *
     * POST /api/admin/crawl/players
     */
    @PostMapping("/players")
    fun crawlPlayers(): ApiResponse<BulkCrawlResult> =
        ApiResponse.ok(bulkCrawlService.crawlAllPlayers())
}
