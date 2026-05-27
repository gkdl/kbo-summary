package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PitcherBasicListParserTest {

    private val parser = PitcherBasicListParser()

    @Test
    fun `Basic1 한 페이지에서 22명의 투수 시즌 기록을 추출한다`() {
        val rows = parser.parse(loadFixture("pitcherBasic1.html"))

        assertThat(rows).hasSize(22)
        val first = rows.first { it.rank == 1 }
        assertThat(first.playerId).isEqualTo("53375")
        assertThat(first.playerName).isEqualTo("후라도")
        assertThat(first.teamCode).isEqualTo("SS") // 삼성 -> SS
        assertThat(first.era).isEqualByComparingTo(BigDecimal("2.40"))
        assertThat(first.games).isEqualTo(10)
        assertThat(first.wins).isEqualTo(2)
        assertThat(first.losses).isEqualTo(1)
        assertThat(first.saves).isEqualTo(0)
        assertThat(first.holds).isEqualTo(0)
        // 63 2/3 -> 63 + 0.67 = 63.67
        assertThat(first.ip).isEqualByComparingTo(BigDecimal("63.67"))
        assertThat(first.hits).isEqualTo(62)
        assertThat(first.so).isEqualTo(46)
        assertThat(first.bb).isEqualTo(10)
    }

    @Test
    fun `빈 HTML 은 빈 목록을 반환한다`() {
        assertThat(parser.parse("<html></html>")).isEmpty()
    }
}
