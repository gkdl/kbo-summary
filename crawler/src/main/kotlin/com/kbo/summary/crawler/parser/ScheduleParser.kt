package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kbo.summary.core.domain.GameStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class GameDto(
    val gameId: String,
    val gameDate: LocalDate,
    val homeTeamCode: String,
    val awayTeamCode: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val startTime: String?,
    val stadium: String?,
    val status: GameStatus,
)

/**
 * KBO 경기 일정 응답 파서 — POST /ws/Main.asmx/GetKboGameList
 *
 * 응답: { "game": [ { "G_ID", "G_DT", "HOME_ID", "AWAY_ID", "S_NM", "G_TM",
 *                     "T_SCORE_CN", "B_SCORE_CN", "GAME_STATE_SC", ... } ], "code", "msg" }
 *
 * GAME_STATE_SC: "1"=경기예정, "2"=경기중, "3"=종료 ('SCHEDULED' | 'IN_PROGRESS' | 'FINISHED')
 * 점수 키: T_SCORE_CN=어웨이(top), B_SCORE_CN=홈(bottom)
 */
@Component
class ScheduleParser(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parseSchedule(json: String): List<GameDto> {
        val games = objectMapper.parseTree(json).path("game")
        if (!games.isArray) return emptyList()
        return games.mapNotNull { node ->
            runCatching { toGameDto(node) }
                .onFailure { log.warn("경기 노드 파싱 실패: {}", it.message) }
                .getOrNull()
        }
    }

    private fun toGameDto(node: JsonNode): GameDto {
        val gameId = node.text("G_ID")
        val gameDate = LocalDate.parse(node.text("G_DT"), DateTimeFormatter.BASIC_ISO_DATE)
        val awayCode = node.text("AWAY_ID")
        val homeCode = node.text("HOME_ID")
        val status = when (node.text("GAME_STATE_SC")) {
            "2" -> GameStatus.IN_PROGRESS
            "3" -> GameStatus.FINISHED
            else -> GameStatus.SCHEDULED
        }
        val finished = status == GameStatus.FINISHED || status == GameStatus.IN_PROGRESS
        return GameDto(
            gameId = gameId,
            gameDate = gameDate,
            homeTeamCode = homeCode,
            awayTeamCode = awayCode,
            homeScore = if (finished) node.intOrNull("B_SCORE_CN") else null,
            awayScore = if (finished) node.intOrNull("T_SCORE_CN") else null,
            startTime = node.textOrNull("G_TM"),
            stadium = node.textOrNull("S_NM"),
            status = status,
        )
    }
}
