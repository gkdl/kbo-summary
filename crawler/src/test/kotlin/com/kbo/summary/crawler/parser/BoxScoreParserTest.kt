package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BoxScoreParserTest {

    private val parser = BoxScoreParser(ObjectMapper())

    @Test
    fun `실제 KBO 박스스코어 응답을 어웨이·홈 타자·투수 목록으로 분해한다`() {
        val box = parser.parseBoxScore(
            gameId = "20260523WOLG0",
            json = loadFixture("boxscoreResponse.json"),
            awayTeamCode = "WO",
            homeTeamCode = "LG",
        )

        assertThat(box.gameId).isEqualTo("20260523WOLG0")

        // 어웨이(키움) 타자 — 라인업 + 교체 합쳐 다수 행
        assertThat(box.awayHitters).isNotEmpty
        val firstAway = box.awayHitters.first()
        assertThat(firstAway.battingOrder).isEqualTo(1)
        assertThat(firstAway.playerName).isEqualTo("서건창")
        assertThat(firstAway.teamCode).isEqualTo("WO")
        assertThat(firstAway.atBats).isEqualTo(4)
        assertThat(firstAway.hits).isEqualTo(1)
        assertThat(firstAway.avg).isEqualByComparingTo(BigDecimal("0.308"))

        // 홈(LG) 타자
        assertThat(box.homeHitters).isNotEmpty
        assertThat(box.homeHitters.first().teamCode).isEqualTo("LG")

        // 어웨이 투수 — 첫 행 = 선발 투수
        assertThat(box.awayPitchers).isNotEmpty
        val starter = box.awayPitchers.first()
        assertThat(starter.playerName).isEqualTo("배동현")
        assertThat(starter.role).isEqualTo("선발")
        assertThat(starter.decision).isEqualTo("패")
        assertThat(starter.inningsPitched).isEqualByComparingTo(BigDecimal("4"))
        assertThat(starter.pitchCount).isEqualTo(69)
        assertThat(starter.hits).isEqualTo(8)        // 피안타
        assertThat(starter.strikeOuts).isEqualTo(3)  // 삼진
        assertThat(starter.earnedRuns).isEqualTo(4)
        assertThat(starter.era).isEqualByComparingTo(BigDecimal("4.54"))
    }
}
