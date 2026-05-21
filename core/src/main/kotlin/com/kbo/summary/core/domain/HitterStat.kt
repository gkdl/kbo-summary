package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "TB_HITTER_STAT")
class HitterStat(
    @Column(name = "PLAYER_ID", length = 20, nullable = false)
    var playerId: String,

    @Column(name = "SEASON", nullable = false)
    var season: Int,

    @Column(name = "AVG", precision = 4, scale = 3)
    var avg: BigDecimal? = null,

    @Column(name = "GAMES", nullable = false)
    var games: Int = 0,

    @Column(name = "AB", nullable = false)
    var ab: Int = 0,

    @Column(name = "HITS", nullable = false)
    var hits: Int = 0,

    @Column(name = "DOUBLES", nullable = false)
    var doubles: Int = 0,

    @Column(name = "TRIPLES", nullable = false)
    var triples: Int = 0,

    @Column(name = "HR", nullable = false)
    var hr: Int = 0,

    @Column(name = "RBI", nullable = false)
    var rbi: Int = 0,

    @Column(name = "RUNS", nullable = false)
    var runs: Int = 0,

    @Column(name = "SB", nullable = false)
    var sb: Int = 0,

    @Column(name = "BB", nullable = false)
    var bb: Int = 0,

    @Column(name = "SO", nullable = false)
    var so: Int = 0,

    @Column(name = "OPS", precision = 5, scale = 3)
    var ops: BigDecimal? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    val id: Long = 0,
)
