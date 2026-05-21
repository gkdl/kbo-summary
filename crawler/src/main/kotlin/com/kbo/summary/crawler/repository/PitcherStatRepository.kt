package com.kbo.summary.crawler.repository

import com.kbo.summary.core.domain.PitcherStat
import org.springframework.data.jpa.repository.JpaRepository

interface PitcherStatRepository : JpaRepository<PitcherStat, Long> {

    fun findByPlayerIdAndSeason(playerId: String, season: Int): PitcherStat?
}
