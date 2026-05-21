package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TeamRosterParserTest {

    private val parser = TeamRosterParser()

    @Test
    fun `로스터 HTML에서 팀 코드와 선수 수를 파싱한다`() {
        val roster = parser.parseRoster(loadFixture("teamRoster.html"))

        assertThat(roster.teamCode).isEqualTo("LG")
        assertThat(roster.players).hasSize(3)
    }

    @Test
    fun `선수 ID·이름·등번호·포지션 그룹을 추출한다`() {
        val roster = parser.parseRoster(loadFixture("teamRoster.html"))

        val pitcher = roster.players.first { it.playerId == "70001" }
        assertThat(pitcher.name).isEqualTo("이영호")
        assertThat(pitcher.backNumber).isEqualTo("21")
        assertThat(pitcher.positionGroup).isEqualTo("투수")

        val infielder = roster.players.first { it.playerId == "60001" }
        assertThat(infielder.name).isEqualTo("홍길동")
        assertThat(infielder.positionGroup).isEqualTo("내야수")
    }
}
