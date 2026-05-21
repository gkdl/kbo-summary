package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "TB_GAME_SCORE")
class GameScore(
    @Column(name = "GAME_ID", length = 20, nullable = false)
    var gameId: String,

    @Column(name = "INNING", nullable = false)
    var inning: Int,

    @Column(name = "HOME_SCORE", nullable = false)
    var homeScore: Int = 0,

    @Column(name = "AWAY_SCORE", nullable = false)
    var awayScore: Int = 0,

    @Column(name = "HOME_R", nullable = false)
    var homeR: Int = 0,

    @Column(name = "HOME_H", nullable = false)
    var homeH: Int = 0,

    @Column(name = "HOME_E", nullable = false)
    var homeE: Int = 0,

    @Column(name = "HOME_B", nullable = false)
    var homeB: Int = 0,

    @Column(name = "AWAY_R", nullable = false)
    var awayR: Int = 0,

    @Column(name = "AWAY_H", nullable = false)
    var awayH: Int = 0,

    @Column(name = "AWAY_E", nullable = false)
    var awayE: Int = 0,

    @Column(name = "AWAY_B", nullable = false)
    var awayB: Int = 0,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    val id: Long = 0,
)
