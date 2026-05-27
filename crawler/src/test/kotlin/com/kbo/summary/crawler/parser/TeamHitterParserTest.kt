package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TeamHitterParserTest {

    private val parser = TeamHitterParser()

    @Test
    fun `팀 타격 통계 페이지에서 10개 팀 데이터를 추출한다`() {
        val rows = parser.parse(loadFixture("teamHitterBasic1.html"))

        assertThat(rows).hasSize(10)
        // KBO 팀 코드 10개가 모두 등장하는지 확인 (집합 비교)
        assertThat(rows.map { it.teamCode }).containsExactlyInAnyOrder(
            "SK", "LG", "OB", "HT", "SS", "LT", "WO", "NC", "KT", "HH",
        )
    }
}
