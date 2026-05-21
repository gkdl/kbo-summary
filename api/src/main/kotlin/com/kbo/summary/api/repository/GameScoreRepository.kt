package com.kbo.summary.api.repository

import com.kbo.summary.core.domain.GameScore
import org.springframework.data.jpa.repository.JpaRepository

interface GameScoreRepository : JpaRepository<GameScore, Long> {

    fun findByGameId(gameId: String): List<GameScore>
}
