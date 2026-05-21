package com.kbo.summary.core.dto

import java.math.BigDecimal

data class TeamDetailDto(
    val teamCode: String,
    val teamName: String,
    val stadium: String?,
    val teamColor: String?,
    val rank: Int?,
    val wins: Int,
    val losses: Int,
    val draws: Int,
)

data class RosterPlayerDto(
    val playerId: String,
    val name: String,
    val backNumber: String?,
    val position: String?,
)

data class TeamRosterDto(
    val teamCode: String,
    val players: List<RosterPlayerDto>,
)

data class TeamStatsDto(
    val teamCode: String,
    val season: Int,
    val rank: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val winRate: BigDecimal?,
    val gamesBehind: BigDecimal?,
)

data class HeadToHeadDto(
    val teamA: String,
    val teamB: String,
    val teamAWins: Int,
    val teamBWins: Int,
    val draws: Int,
    val games: List<GameDto>,
)

data class RecentFormDto(
    val teamCode: String,
    val recentForm: List<String>,
)
