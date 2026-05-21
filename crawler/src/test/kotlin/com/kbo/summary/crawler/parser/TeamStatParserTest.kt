package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TeamStatParserTest {

    private val parser = TeamStatParser(ObjectMapper())

    @Test
    fun `팀 타자 기록을 파싱한다`() {
        val stat = parser.parseTeamHitterStat(loadFixture("teamHitterStat.json"))

        assertThat(stat.teamCode).isEqualTo("LG")
        assertThat(stat.season).isEqualTo(2024)
        assertThat(stat.games).isEqualTo(144)
        assertThat(stat.homeRuns).isEqualTo(130)
        assertThat(stat.stolenBases).isEqualTo(102)
        assertThat(stat.avg).isEqualByComparingTo(BigDecimal("0.276"))
        assertThat(stat.ops).isEqualByComparingTo(BigDecimal("0.768"))
    }

    @Test
    fun `팀 투수 기록을 파싱한다`() {
        val stat = parser.parseTeamPitcherStat(loadFixture("teamPitcherStat.json"))

        assertThat(stat.teamCode).isEqualTo("LG")
        assertThat(stat.wins).isEqualTo(86)
        assertThat(stat.saves).isEqualTo(38)
        assertThat(stat.inningsPitched).isEqualTo("1278.1")
        assertThat(stat.era).isEqualByComparingTo(BigDecimal("4.12"))
        assertThat(stat.whip).isEqualByComparingTo(BigDecimal("1.38"))
    }
}
