package com.kbo.summary.core.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class GameDto(
    val gameId: String,
    val gameDate: LocalDate,
    val homeTeamCode: String,
    val awayTeamCode: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: String,
    val stadium: String?,
    val startTime: String?,
)

data class InningScoreDto(
    val inning: Int,
    val homeRuns: Int,
    val awayRuns: Int,
)

data class TeamLineDto(
    val runs: Int,
    val hits: Int,
    val errors: Int,
    val walks: Int,
)

data class GameDetailDto(
    val game: GameDto,
    val inningScores: List<InningScoreDto>,
    val homeLine: TeamLineDto,
    val awayLine: TeamLineDto,
)

data class GameSummaryDto(
    val gameId: String,
    val summary: String,
    val createdAt: LocalDateTime,
)
