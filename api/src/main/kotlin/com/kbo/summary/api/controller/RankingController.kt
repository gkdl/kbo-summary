package com.kbo.summary.api.controller

import com.kbo.summary.api.service.RankingService
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.dto.StandingDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rankings")
class RankingController(
    private val rankingService: RankingService,
) {
    @GetMapping
    @Cacheable("standings")
    fun getRankings(): ApiResponse<List<StandingDto>> =
        ApiResponse.ok(rankingService.getStandings())
}
