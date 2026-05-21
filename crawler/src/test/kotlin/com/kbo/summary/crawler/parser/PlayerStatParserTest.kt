package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PlayerStatParserTest {

    private val parser = PlayerStatParser(ObjectMapper())

    @Test
    fun `타자 시즌 기록을 파싱한다`() {
        val stat = parser.parseHitterStat(loadFixture("hitterStat.json"))

        assertThat(stat.playerId).isEqualTo("60001")
        assertThat(stat.season).isEqualTo(2024)
        assertThat(stat.atBats).isEqualTo(520)
        assertThat(stat.hits).isEqualTo(162)
        assertThat(stat.doubles).isEqualTo(30)
        assertThat(stat.triples).isEqualTo(2)
        assertThat(stat.homeRuns).isEqualTo(25)
        assertThat(stat.stolenBases).isEqualTo(12)
        assertThat(stat.avg).isEqualByComparingTo(BigDecimal("0.312"))
        assertThat(stat.ops).isEqualByComparingTo(BigDecimal("0.890"))
    }

    @Test
    fun `투수 시즌 기록을 파싱한다`() {
        val stat = parser.parsePitcherStat(loadFixture("pitcherStat.json"))

        assertThat(stat.playerId).isEqualTo("70001")
        assertThat(stat.season).isEqualTo(2024)
        assertThat(stat.wins).isEqualTo(15)
        assertThat(stat.losses).isEqualTo(6)
        assertThat(stat.inningsPitched).isEqualTo("180.2")
        assertThat(stat.strikeOuts).isEqualTo(175)
        assertThat(stat.era).isEqualByComparingTo(BigDecimal("2.85"))
        assertThat(stat.whip).isEqualByComparingTo(BigDecimal("1.08"))
    }
}
