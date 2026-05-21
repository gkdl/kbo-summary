package com.kbo.summary.crawler.repository

import com.kbo.summary.core.domain.Standing
import org.springframework.data.jpa.repository.JpaRepository

interface StandingRepository : JpaRepository<Standing, Long> {

    fun findBySeasonOrderByRank(season: Int): List<Standing>
}
