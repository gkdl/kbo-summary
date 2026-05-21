package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "TB_STANDING")
class Standing(
    @Column(name = "SEASON", nullable = false)
    var season: Int,

    @Column(name = "TEAM_CODE", length = 10, nullable = false)
    var teamCode: String,

    @Column(name = "TEAM_RANK", nullable = false)
    var rank: Int,

    @Column(name = "WINS", nullable = false)
    var wins: Int = 0,

    @Column(name = "LOSSES", nullable = false)
    var losses: Int = 0,

    @Column(name = "DRAWS", nullable = false)
    var draws: Int = 0,

    @Column(name = "WIN_RATE", precision = 4, scale = 3)
    var winRate: BigDecimal? = null,

    @Column(name = "GAMES_BEHIND", precision = 4, scale = 1)
    var gamesBehind: BigDecimal? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    val id: Long = 0,
)
