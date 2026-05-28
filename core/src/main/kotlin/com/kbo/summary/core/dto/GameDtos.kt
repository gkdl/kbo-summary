package com.kbo.summary.core.dto

import java.math.BigDecimal
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

data class BoxHitterDto(
    val playerName: String,
    val battingOrder: Int?,
    val position: String?,
    val teamCode: String,
    val atBats: Int,
    val hits: Int,
    val rbi: Int,
    val runs: Int,
    val avg: BigDecimal?,
)

data class BoxPitcherDto(
    val playerName: String,
    val teamCode: String,
    val role: String?,
    val decision: String?,
    val inningsPitched: BigDecimal?,
    val pitchCount: Int,
    val battersFaced: Int,
    val atBats: Int,
    val hits: Int,
    val homeRuns: Int,
    val walks: Int,
    val strikeOuts: Int,
    val runs: Int,
    val earnedRuns: Int,
    val era: BigDecimal?,
)

data class HighlightDto(
    val youtubeVideoId: String,
    val title: String?,
)

/**
 * 하이라이트 목록 화면용 — 게임 메타(스코어, 팀) + 하이라이트.
 * /api/games/highlights?date=YYYYMMDD 응답 element.
 */
data class GameHighlightDto(
    val gameId: String,
    val gameDate: String,
    val awayTeamCode: String,
    val homeTeamCode: String,
    val awayScore: Int?,
    val homeScore: Int?,
    val highlight: HighlightDto,
)

data class GameDetailDto(
    val game: GameDto,
    val inningScores: List<InningScoreDto>,
    val homeLine: TeamLineDto,
    val awayLine: TeamLineDto,
    val awayHitters: List<BoxHitterDto> = emptyList(),
    val homeHitters: List<BoxHitterDto> = emptyList(),
    val awayPitchers: List<BoxPitcherDto> = emptyList(),
    val homePitchers: List<BoxPitcherDto> = emptyList(),
    val highlight: HighlightDto? = null,
)

data class GameSummaryDto(
    val gameId: String,
    val summary: String,
    val createdAt: LocalDateTime,
)
