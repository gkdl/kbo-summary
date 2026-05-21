package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerGameLogParserTest {

    private val parser = PlayerGameLogParser()

    @Test
    fun `타자 경기별 기록을 파싱한다`() {
        val logs = parser.parseHitterGameLog(loadFixture("playerGameLogHitter.html"))

        assertThat(logs).hasSize(3)
        val first = logs[0]
        assertThat(first.gameDate).isEqualTo("05.01")
        assertThat(first.opponent).isEqualTo("두산")
        assertThat(first.atBats).isEqualTo(4)
        assertThat(first.hits).isEqualTo(2)
        assertThat(first.rbi).isEqualTo(1)
        assertThat(logs[2].homeRuns).isEqualTo(1)
    }

    @Test
    fun `투수 경기별 기록을 파싱한다`() {
        val logs = parser.parsePitcherGameLog(loadFixture("playerGameLogPitcher.html"))

        assertThat(logs).hasSize(2)
        val first = logs[0]
        assertThat(first.gameDate).isEqualTo("05.01")
        assertThat(first.result).isEqualTo("승")
        assertThat(first.inningsPitched).isEqualTo("6.0")
        assertThat(first.runs).isEqualTo(3)
        assertThat(first.earnedRuns).isEqualTo(2)
        assertThat(first.strikeOuts).isEqualTo(7)
    }
}
