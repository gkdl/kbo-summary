package com.kbo.summary.crawler.repository

import com.kbo.summary.core.domain.HitterStat
import org.springframework.data.jpa.repository.JpaRepository

interface HitterStatRepository : JpaRepository<HitterStat, Long> {

    fun findByPlayerIdAndSeason(playerId: String, season: Int): HitterStat?
}
