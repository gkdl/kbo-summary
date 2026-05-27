package com.kbo.summary.api.service

import com.kbo.summary.core.domain.Standing
import com.kbo.summary.core.dto.StandingDto
import com.kbo.summary.crawler.repository.StandingRepository
import com.kbo.summary.crawler.service.TeamCrawlerService
import org.springframework.stereotype.Service

@Service
class RankingService(
    private val standingRepository: StandingRepository,
    private val teamCrawlerService: TeamCrawlerService,
) {
    fun getStandings(): List<StandingDto> {
        var standings = standingRepository.findBySeasonOrderByRank(currentSeason())
        if (standings.isEmpty()) {
            crawlSafely { teamCrawlerService.crawlStandings() }
            standings = standingRepository.findBySeasonOrderByRank(currentSeason())
        }
        return standings.map { it.toDto() }
    }

    private fun Standing.toDto(): StandingDto =
        StandingDto(
            rank = rank,
            teamCode = teamCode,
            season = season,
            wins = wins,
            losses = losses,
            draws = draws,
            winRate = winRate,
            gamesBehind = gamesBehind,
        )
}
