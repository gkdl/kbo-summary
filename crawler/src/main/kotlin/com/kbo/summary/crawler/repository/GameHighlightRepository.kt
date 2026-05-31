package com.kbo.summary.crawler.repository

import com.kbo.summary.core.domain.GameHighlight
import org.springframework.data.jpa.repository.JpaRepository

interface GameHighlightRepository : JpaRepository<GameHighlight, String> {
    fun findByGameId(gameId: String): GameHighlight?
}
