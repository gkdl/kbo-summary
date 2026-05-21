package com.kbo.summary.crawler.repository

import com.kbo.summary.core.domain.Player
import org.springframework.data.jpa.repository.JpaRepository

interface PlayerRepository : JpaRepository<Player, String> {

    fun findByTeamCode(teamCode: String): List<Player>

    fun findByPlayerNameContaining(keyword: String): List<Player>
}
