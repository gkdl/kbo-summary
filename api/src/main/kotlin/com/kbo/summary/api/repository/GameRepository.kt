package com.kbo.summary.api.repository

import com.kbo.summary.core.domain.Game
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface GameRepository : JpaRepository<Game, String> {

    fun findByGameDate(gameDate: LocalDate): List<Game>

    fun findByGameId(gameId: String): Game?
}
