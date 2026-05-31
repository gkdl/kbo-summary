package com.kbo.summary.crawler.repository

import com.kbo.summary.core.domain.GameBoxHitter
import org.springframework.data.jpa.repository.JpaRepository

interface GameBoxHitterRepository : JpaRepository<GameBoxHitter, Long> {
    fun findByGameId(gameId: String): List<GameBoxHitter>
    fun deleteByGameId(gameId: String)
}
