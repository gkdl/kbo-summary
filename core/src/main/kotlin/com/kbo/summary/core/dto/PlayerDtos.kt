package com.kbo.summary.core.dto

import java.math.BigDecimal
import java.time.LocalDate

data class PlayerProfileDto(
    val playerId: String,
    val name: String,
    val teamCode: String?,
    val playerType: String,
    val position: String?,
    val backNumber: String?,
    val bats: String?,
    val throws: String?,
    val birthDate: LocalDate?,
    val height: Int?,
    val weight: Int?,
    val school: String?,
    val debutYear: Int?,
)

data class HittingLine(
    val avg: BigDecimal?,
    val games: Int,
    val atBats: Int,
    val hits: Int,
    val homeRuns: Int,
    val rbi: Int,
    val runs: Int,
    val stolenBases: Int,
    val walks: Int,
    val strikeOuts: Int,
    val ops: BigDecimal?,
)

data class PitchingLine(
    val era: BigDecimal?,
    val games: Int,
    val wins: Int,
    val losses: Int,
    val saves: Int,
    val holds: Int,
    val inningsPitched: BigDecimal?,
    val hits: Int,
    val strikeOuts: Int,
    val walks: Int,
    val whip: BigDecimal?,
)

data class PlayerStatDto(
    val playerId: String,
    val season: Int,
    val playerType: String,
    val hitting: HittingLine?,
    val pitching: PitchingLine?,
)

data class PlayerSearchResultDto(
    val playerId: String,
    val name: String,
    val teamCode: String?,
    val playerType: String,
    val position: String?,
)

data class PlayerRankingDto(
    val rank: Int,
    val playerId: String,
    val playerName: String,
    val teamCode: String?,
    val category: String,
    val value: String,
)
