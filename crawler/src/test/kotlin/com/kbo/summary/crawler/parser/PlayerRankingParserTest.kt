package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerRankingParserTest {

    private val parser = PlayerRankingParser(ObjectMapper())

    @Test
    fun `타자 순위를 PlayerRankingDto 목록으로 파싱한다`() {
        val ranking = parser.parseHitterRanking(loadFixture("hitterRanking.json"))

        assertThat(ranking).hasSize(2)
        val first = ranking[0]
        assertThat(first.rank).isEqualTo(1)
        assertThat(first.playerId).isEqualTo("60001")
        assertThat(first.playerName).isEqualTo("홍길동")
        assertThat(first.teamCode).isEqualTo("LG")
        assertThat(first.value).isEqualTo("0.312")
    }

    @Test
    fun `투수 순위를 파싱하고 팀명을 코드로 변환한다`() {
        val ranking = parser.parsePitcherRanking(loadFixture("pitcherRanking.json"))

        assertThat(ranking).hasSize(2)
        assertThat(ranking[0].playerId).isEqualTo("70001")
        assertThat(ranking[0].teamCode).isEqualTo("SS")
        assertThat(ranking[1].teamCode).isEqualTo("KT")
    }
}
