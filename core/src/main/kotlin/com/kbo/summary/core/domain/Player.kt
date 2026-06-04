package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

enum class PlayerType {
    HITTER,
    PITCHER,
}

@Entity
@Table(name = "TB_PLAYER")
class Player(
    @Id
    @Column(name = "PLAYER_ID", length = 20)
    val playerId: String,

    @Column(name = "PLAYER_NAME", length = 50, nullable = false)
    var playerName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "PLAYER_TYPE", length = 10, nullable = false)
    var playerType: PlayerType,

    @Column(name = "TEAM_CODE", length = 10)
    var teamCode: String? = null,

    @Column(name = "POSITION", length = 20)
    var position: String? = null,

    @Column(name = "BACK_NUMBER", length = 5)
    var backNumber: String? = null,
)
