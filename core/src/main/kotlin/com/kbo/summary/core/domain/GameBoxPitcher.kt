package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "TB_GAME_BOX_PITCHER")
class GameBoxPitcher(
    @Column(name = "GAME_ID", length = 20, nullable = false)
    val gameId: String,

    @Column(name = "TEAM_CODE", length = 10, nullable = false)
    val teamCode: String,

    @Column(name = "PLAYER_NAME", length = 50, nullable = false)
    val playerName: String,

    @Column(name = "ROLE", length = 20)
    val role: String? = null,

    @Column(name = "DECISION", length = 10)
    val decision: String? = null,

    @Column(name = "WINS", nullable = false)
    val wins: Int = 0,

    @Column(name = "LOSSES", nullable = false)
    val losses: Int = 0,

    @Column(name = "SAVES", nullable = false)
    val saves: Int = 0,

    @Column(name = "INNINGS_PITCHED", precision = 5, scale = 1)
    val inningsPitched: BigDecimal? = null,

    @Column(name = "BATTERS_FACED", nullable = false)
    val battersFaced: Int = 0,

    @Column(name = "PITCH_COUNT", nullable = false)
    val pitchCount: Int = 0,

    @Column(name = "AT_BATS", nullable = false)
    val atBats: Int = 0,

    @Column(name = "HITS", nullable = false)
    val hits: Int = 0,

    @Column(name = "HOME_RUNS", nullable = false)
    val homeRuns: Int = 0,

    @Column(name = "WALKS", nullable = false)
    val walks: Int = 0,

    @Column(name = "STRIKE_OUTS", nullable = false)
    val strikeOuts: Int = 0,

    @Column(name = "RUNS", nullable = false)
    val runs: Int = 0,

    @Column(name = "EARNED_RUNS", nullable = false)
    val earnedRuns: Int = 0,

    @Column(name = "ERA", precision = 5, scale = 2)
    val era: BigDecimal? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    val id: Long = 0,
)
