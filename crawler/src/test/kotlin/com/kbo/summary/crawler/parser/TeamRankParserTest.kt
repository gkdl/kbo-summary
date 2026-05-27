package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Year

class TeamRankParserTest {

    private val parser = TeamRankParser()

    @Test
    fun `TeamRankDaily 첫 테이블에서 10개 팀 순위를 추출한다`() {
        val rows = parser.parse(loadFixture("teamRankDaily.html"))

        assertThat(rows).hasSize(10)
        val first = rows.first()
        assertThat(first.season).isEqualTo(Year.now().value)
        assertThat(first.rank).isEqualTo(1)
        assertThat(first.teamCode).isEqualTo("SS") // 삼성 -> SS
        assertThat(first.wins).isEqualTo(27)
        assertThat(first.losses).isEqualTo(18)
        assertThat(first.draws).isEqualTo(1)
        assertThat(first.winRate).isEqualByComparingTo(BigDecimal("0.600"))
        assertThat(first.gamesBehind).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `빈 HTML 은 빈 목록을 반환한다`() {
        assertThat(parser.parse("<html></html>")).isEmpty()
    }
}
