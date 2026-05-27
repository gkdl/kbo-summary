package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class HitterBasicListParserTest {

    private val parser = HitterBasicListParser()

    @Test
    fun `Basic1 + Basic2 를 머지해 한 페이지 분량의 통계를 반환한다`() {
        val basic1 = loadFixture("hitterBasic1.html")
        val basic2 = loadFixture("hitterBasic2.html")

        val rows = parser.parse(basic1, basic2)

        // KBO 페이지는 디폴트 30명 표시. 양쪽 페이지가 같은 정렬·페이지면 머지 결과는 동일 인원수.
        assertThat(rows).hasSize(30)
        val first = rows.first { it.rank == 1 }
        assertThat(first.playerId).isEqualTo("67893")
        assertThat(first.playerName).isEqualTo("박성한")
        assertThat(first.teamCode).isEqualTo("SK") // SSG -> SK
        assertThat(first.avg).isEqualByComparingTo(BigDecimal("0.371"))
        assertThat(first.games).isEqualTo(47)
        assertThat(first.ab).isEqualTo(175)
        assertThat(first.hits).isEqualTo(65)
        assertThat(first.hr).isEqualTo(3)
        assertThat(first.rbi).isEqualTo(29)
        assertThat(first.runs).isEqualTo(35)
        // Basic2 머지된 필드
        assertThat(first.bb).isEqualTo(35)
        assertThat(first.so).isEqualTo(20)
        assertThat(first.ops).isEqualByComparingTo(BigDecimal("0.971"))
    }

    @Test
    fun `빈 HTML 은 빈 목록을 반환한다`() {
        assertThat(parser.parse("<html></html>", "<html></html>")).isEmpty()
    }
}
