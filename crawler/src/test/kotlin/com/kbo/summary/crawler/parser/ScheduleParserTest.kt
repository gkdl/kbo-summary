package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.kbo.summary.core.domain.GameStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal fun loadFixture(fileName: String): String =
    object {}.javaClass.getResource("/fixtures/$fileName")?.readText()
        ?: throw IllegalStateException("fixture를 찾을 수 없습니다: $fileName")

class ScheduleParserTest {

    private val parser = ScheduleParser(ObjectMapper())

    @Test
    fun `경기 일정 JSON을 GameDto 목록으로 파싱한다`() {
        val games = parser.parseSchedule(loadFixture("scheduleResponse.json"))

        assertThat(games).hasSize(2)
        val first = games[0]
        assertThat(first.gameId).isEqualTo("20240601LGOB0")
        assertThat(first.gameDate).isEqualTo(LocalDate.of(2024, 6, 1))
        assertThat(first.awayTeamCode).isEqualTo("LG")
        assertThat(first.homeTeamCode).isEqualTo("OB")
        assertThat(first.startTime).isEqualTo("18:30")
        assertThat(first.stadium).isEqualTo("잠실")
        assertThat(first.status).isEqualTo(GameStatus.SCHEDULED)
    }

    @Test
    fun `상태 코드를 GameStatus로 변환하고 gameId를 조합한다`() {
        val games = parser.parseSchedule(loadFixture("scheduleResponse.json"))

        assertThat(games[1].status).isEqualTo(GameStatus.FINISHED)
        assertThat(games[1].gameId).isEqualTo("20240601HHSS0")
    }

    @Test
    fun `배열이 없는 JSON은 빈 목록을 반환한다`() {
        assertThat(parser.parseSchedule("{}")).isEmpty()
    }
}
