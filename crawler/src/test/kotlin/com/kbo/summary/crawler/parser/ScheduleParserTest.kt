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
    fun `GetKboGameList 응답을 GameDto 목록으로 파싱한다`() {
        val games = parser.parseSchedule(loadFixture("gameListResponse.json"))

        assertThat(games).isNotEmpty
        val first = games.first()
        assertThat(first.gameId).isEqualTo("20260524WOLG0")
        assertThat(first.gameDate).isEqualTo(LocalDate.of(2026, 5, 24))
        assertThat(first.awayTeamCode).isEqualTo("WO")
        assertThat(first.homeTeamCode).isEqualTo("LG")
        assertThat(first.startTime).isEqualTo("14:00")
        // GAME_STATE_SC=1 (경기 예정) 이므로 점수는 null
        assertThat(first.status).isEqualTo(GameStatus.SCHEDULED)
        assertThat(first.homeScore).isNull()
        assertThat(first.awayScore).isNull()
    }

    @Test
    fun `game 키가 없는 JSON은 빈 목록을 반환한다`() {
        assertThat(parser.parseSchedule("{}")).isEmpty()
    }

    @Test
    fun `잘못된 JSON은 빈 목록 또는 예외를 던지지 않고 처리된다`() {
        assertThat(parser.parseSchedule("""{"game": []}""")).isEmpty()
    }
}
