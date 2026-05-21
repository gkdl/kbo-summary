package com.kbo.summary.crawler.repository

import com.kbo.summary.core.domain.Team
import org.springframework.data.jpa.repository.JpaRepository

interface TeamRepository : JpaRepository<Team, String>
