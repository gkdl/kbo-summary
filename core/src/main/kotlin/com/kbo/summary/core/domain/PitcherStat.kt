package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "TB_PITCHER_STAT")
class PitcherStat(
    @Column(name = "PLAYER_ID", length = 20, nullable = false)
    var playerId: String,

    @Column(name = "SEASON", nullable = false)
    var season: Int,

    @Column(name = "ERA", precision = 5, scale = 2)
    var era: BigDecimal? = null,

    @Column(name = "GAMES", nullable = false)
    var games: Int = 0,

    @Column(name = "WINS", nullable = false)
    var wins: Int = 0,

    @Column(name = "LOSSES", nullable = false)
    var losses: Int = 0,

    @Column(name = "SAVES", nullable = false)
    var saves: Int = 0,

    @Column(name = "HOLDS", nullable = false)
    var holds: Int = 0,

    @Column(name = "IP", precision = 6, scale = 1)
    var ip: BigDecimal? = null,

    @Column(name = "HITS", nullable = false)
    var hits: Int = 0,

    @Column(name = "SO", nullable = false)
    var so: Int = 0,

    @Column(name = "BB", nullable = false)
    var bb: Int = 0,

    @Column(name = "WHIP", precision = 4, scale = 2)
    var whip: BigDecimal? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    val id: Long = 0,
)
