package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BoxScoreParserTest {

    private val parser = BoxScoreParser(ObjectMapper())

    @Test
    fun `박스스코어 JSON의 승패투수와 홈런을 파싱한다`() {
        val box = parser.parseBoxScore(loadFixture("boxscoreResponse.json"))

        assertThat(box.gameId).isEqualTo("20240601LGOB0")
        assertThat(box.hitters).hasSize(2)
        assertThat(box.pitchers).hasSize(2)
        assertThat(box.winningPitcherId).isEqualTo("70001")
        assertThat(box.losingPitcherId).isEqualTo("70002")
        assertThat(box.savePitcherId).isEqualTo("70003")
        assertThat(box.homeRunHitters).containsExactly("김철수(1호)")
    }

    @Test
    fun `타자 기록을 파싱한다`() {
        val box = parser.parseBoxScore(loadFixture("boxscoreResponse.json"))

        val hitter = box.hitters.first { it.playerId == "60002" }
        assertThat(hitter.playerName).isEqualTo("김철수")
        assertThat(hitter.teamCode).isEqualTo("LG")
        assertThat(hitter.atBats).isEqualTo(4)
        assertThat(hitter.homeRuns).isEqualTo(1)
        assertThat(hitter.rbi).isEqualTo(2)
    }

    @Test
    fun `투수 기록과 승패 결정을 파싱한다`() {
        val box = parser.parseBoxScore(loadFixture("boxscoreResponse.json"))

        val winner = box.pitchers.first { it.decision == "WIN" }
        assertThat(winner.playerId).isEqualTo("70001")
        assertThat(winner.inningsPitched).isEqualTo("6.0")
        assertThat(winner.earnedRuns).isEqualTo(2)
        assertThat(winner.strikeOuts).isEqualTo(7)
    }
}
