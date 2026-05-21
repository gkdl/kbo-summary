package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

enum class GameStatus {
    SCHEDULED,
    IN_PROGRESS,
    FINISHED,
}

@Entity
@Table(name = "TB_GAME")
class Game(
    @Id
    @Column(name = "GAME_ID", length = 20)
    val gameId: String,

    @Column(name = "GAME_DATE", nullable = false)
    var gameDate: LocalDate,

    @Column(name = "HOME_TEAM_CODE", length = 10, nullable = false)
    var homeTeamCode: String,

    @Column(name = "AWAY_TEAM_CODE", length = 10, nullable = false)
    var awayTeamCode: String,

    @Column(name = "HOME_SCORE")
    var homeScore: Int? = null,

    @Column(name = "AWAY_SCORE")
    var awayScore: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    var status: GameStatus = GameStatus.SCHEDULED,

    @Column(name = "STADIUM", length = 50)
    var stadium: String? = null,

    @Column(name = "START_TIME", length = 10)
    var startTime: String? = null,
)
