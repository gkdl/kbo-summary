package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class StandingsParserTest {

    private val parser = StandingsParser(ObjectMapper())

    @Test
    fun `순위 JSON을 StandingDto 목록으로 파싱한다`() {
        val standings = parser.parseStandings(loadFixture("standingsResponse.json"))

        assertThat(standings).hasSize(2)
        val first = standings[0]
        assertThat(first.rank).isEqualTo(1)
        assertThat(first.season).isEqualTo(2024)
        assertThat(first.teamCode).isEqualTo("LG")
        assertThat(first.wins).isEqualTo(86)
        assertThat(first.losses).isEqualTo(56)
        assertThat(first.draws).isEqualTo(2)
        assertThat(first.winRate).isEqualByComparingTo(BigDecimal("0.606"))
        assertThat(first.gamesBehind).isEqualByComparingTo(BigDecimal("0.0"))
    }

    @Test
    fun `팀명을 팀 코드로 변환한다`() {
        val standings = parser.parseStandings(loadFixture("standingsResponse.json"))

        assertThat(standings[1].teamCode).isEqualTo("OB")
    }
}
