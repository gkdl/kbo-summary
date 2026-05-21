package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerSearchParserTest {

    private val parser = PlayerSearchParser()

    @Test
    fun `선수 검색 결과 HTML을 파싱한다`() {
        val results = parser.parseSearch(loadFixture("playerSearch.html"))

        assertThat(results).hasSize(2)
        val first = results[0]
        assertThat(first.playerId).isEqualTo("60001")
        assertThat(first.name).isEqualTo("홍길동")
        assertThat(first.teamCode).isEqualTo("LG")
        assertThat(first.position).isEqualTo("내야수")
    }

    @Test
    fun `검색 결과의 팀과 포지션을 추출한다`() {
        val results = parser.parseSearch(loadFixture("playerSearch.html"))

        val pitcher = results.first { it.playerId == "70001" }
        assertThat(pitcher.teamCode).isEqualTo("SS")
        assertThat(pitcher.position).isEqualTo("투수")
    }
}
