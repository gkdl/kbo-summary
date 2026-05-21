package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "TB_GAME_SUMMARY")
class GameSummary(
    @Id
    @Column(name = "GAME_ID", length = 20)
    val gameId: String,

    @Lob
    @Column(name = "SUMMARY", nullable = false)
    var summary: String,

    @Column(name = "CREATED_AT", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
)
