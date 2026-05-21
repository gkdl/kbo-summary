package com.kbo.summary.core.dto

import java.math.BigDecimal

data class StandingDto(
    val rank: Int,
    val teamCode: String,
    val season: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val winRate: BigDecimal?,
    val gamesBehind: BigDecimal?,
)
