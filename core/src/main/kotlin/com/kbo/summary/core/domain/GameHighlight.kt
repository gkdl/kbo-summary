package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "TB_GAME_HIGHLIGHT")
class GameHighlight(
    @Id
    @Column(name = "GAME_ID", length = 20)
    val gameId: String,

    @Column(name = "YOUTUBE_VIDEO_ID", length = 50, nullable = false)
    var youtubeVideoId: String,

    @Column(name = "TITLE", length = 200)
    var title: String? = null,

    @Column(name = "CREATED_AT", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
