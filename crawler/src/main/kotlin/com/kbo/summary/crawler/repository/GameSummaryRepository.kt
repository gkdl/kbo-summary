package com.kbo.summary.crawler.repository

import com.kbo.summary.core.domain.GameSummary
import org.springframework.data.jpa.repository.JpaRepository

interface GameSummaryRepository : JpaRepository<GameSummary, String> {

    fun findByGameId(gameId: String): GameSummary?
}
