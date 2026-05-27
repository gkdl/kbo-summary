package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TeamPitcherParserTest {

    private val parser = TeamPitcherParser()

    @Test
    fun `팀 투수 통계 페이지에서 10개 팀 데이터를 추출한다`() {
        val rows = parser.parse(loadFixture("teamPitcherBasic1.html"))

        assertThat(rows).hasSize(10)
        assertThat(rows.map { it.teamCode }).containsExactlyInAnyOrder(
            "SK", "LG", "OB", "HT", "SS", "LT", "WO", "NC", "KT", "HH",
        )
    }
}
