package com.kbo.summary.crawler.repository

import com.kbo.summary.core.domain.GameBoxPitcher
import org.springframework.data.jpa.repository.JpaRepository

interface GameBoxPitcherRepository : JpaRepository<GameBoxPitcher, Long> {
    fun findByGameId(gameId: String): List<GameBoxPitcher>
    fun deleteByGameId(gameId: String)
}
