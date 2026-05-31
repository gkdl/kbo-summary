package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "TB_GAME_BOX_HITTER")
class GameBoxHitter(
    @Column(name = "GAME_ID", length = 20, nullable = false)
    val gameId: String,

    @Column(name = "TEAM_CODE", length = 10, nullable = false)
    val teamCode: String,

    @Column(name = "PLAYER_NAME", length = 50, nullable = false)
    val playerName: String,

    @Column(name = "BATTING_ORDER")
    val battingOrder: Int? = null,

    @Column(name = "POSITION", length = 10)
    val position: String? = null,

    @Column(name = "AT_BATS", nullable = false)
    val atBats: Int = 0,

    @Column(name = "HITS", nullable = false)
    val hits: Int = 0,

    @Column(name = "RBI", nullable = false)
    val rbi: Int = 0,

    @Column(name = "RUNS", nullable = false)
    val runs: Int = 0,

    @Column(name = "AVG", precision = 5, scale = 3)
    val avg: BigDecimal? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    val id: Long = 0,
)
