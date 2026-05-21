package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "TB_TEAM")
class Team(
    @Id
    @Column(name = "TEAM_CODE", length = 10)
    val teamCode: String,

    @Column(name = "TEAM_NAME", length = 50, nullable = false)
    var teamName: String,

    @Column(name = "STADIUM", length = 50)
    var stadium: String? = null,

    @Column(name = "TEAM_COLOR", length = 20)
    var teamColor: String? = null,
)
