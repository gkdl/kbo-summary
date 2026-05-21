package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kbo.summary.core.domain.GameStatus
import com.kbo.summary.core.exception.CrawlerException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 주의: 사양서대로 KIA·한화가 모두 "HH" 로 매핑됨 — 코드 충돌. KIA는 통상 "HT" 사용 권장.
val KBO_TEAM_CODES: Map<String, String> = mapOf(
    "LG" to "LG",
    "KT" to "KT",
    "SSG" to "SK",
    "NC" to "NC",
    "두산" to "OB",
    "KIA" to "HH",
    "롯데" to "LT",
    "삼성" to "SS",
    "한화" to "HH",
    "키움" to "WO",
)

fun toTeamCode(teamName: String): String {
    val key = teamName.trim()
    if (key.isEmpty()) return ""
    KBO_TEAM_CODES[key]?.let { return it }
    KBO_TEAM_CODES.entries.firstOrNull { key.contains(it.key) }?.let { return it.value }
    return key.uppercase()
}

internal fun ObjectMapper.parseTree(json: String): JsonNode =
    try {
        readTree(json)
    } catch (e: Exception) {
        throw CrawlerException("KBO 응답 JSON 파싱 실패", e)
    }

internal fun JsonNode.firstArrayOf(vararg fieldNames: String): List<JsonNode> {
    if (isArray) return toList()
    for (name in fieldNames) {
        val node = path(name)
        if (node.isArray) return node.toList()
    }
    return emptyList()
}

internal fun JsonNode.textOrNull(vararg fieldNames: String): String? {
    for (name in fieldNames) {
        val node = path(name)
        if (!node.isMissingNode && !node.isNull) {
            val value = node.asText().trim()
            if (value.isNotEmpty()) return value
        }
    }
    return null
}

internal fun JsonNode.text(vararg fieldNames: String): String =
    textOrNull(*fieldNames) ?: ""

internal fun JsonNode.intOrNull(vararg fieldNames: String): Int? {
    for (name in fieldNames) {
        val node = path(name)
        if (!node.isMissingNode && !node.isNull) {
            if (node.isNumber) return node.asInt()
            node.asText().trim().replace(",", "").toIntOrNull()?.let { return it }
        }
    }
    return null
}

internal fun JsonNode.intOf(vararg fieldNames: String): Int =
    intOrNull(*fieldNames) ?: 0

internal fun JsonNode.decimalOrNull(vararg fieldNames: String): BigDecimal? {
    for (name in fieldNames) {
        val node = path(name)
        if (!node.isMissingNode && !node.isNull) {
            val value = node.asText().trim()
            if (value.isNotEmpty()) {
                runCatching { return BigDecimal(value) }
            }
        }
    }
    return null
}

private val DATE_FORMATTERS = listOf(
    DateTimeFormatter.BASIC_ISO_DATE,
    DateTimeFormatter.ISO_LOCAL_DATE,
    DateTimeFormatter.ofPattern("yyyy.MM.dd"),
    DateTimeFormatter.ofPattern("yyyy/MM/dd"),
)

internal fun parseGameDate(raw: String?): LocalDate {
    val value = raw?.trim().orEmpty()
    if (value.isNotEmpty()) {
        for (formatter in DATE_FORMATTERS) {
            runCatching { return LocalDate.parse(value, formatter) }
        }
        val digits = value.filter(Char::isDigit)
        if (digits.length == 8) {
            runCatching { return LocalDate.parse(digits, DateTimeFormatter.BASIC_ISO_DATE) }
        }
    }
    throw CrawlerException("경기 날짜를 해석할 수 없습니다: '$raw'")
}

// gameId = YYYYMMDD + 어웨이코드 + 홈코드 + "0"
internal fun buildGameId(gameDate: LocalDate, awayTeamCode: String, homeTeamCode: String): String =
    gameDate.format(DateTimeFormatter.BASIC_ISO_DATE) + awayTeamCode + homeTeamCode + "0"

internal fun toGameStatus(raw: String?): GameStatus {
    val value = raw?.trim()?.uppercase().orEmpty()
    return when {
        value.isEmpty() -> GameStatus.SCHEDULED
        listOf("RESULT", "END", "FINAL", "종료", "결과").any(value::contains) -> GameStatus.FINISHED
        listOf("LIVE", "PLAYING", "경기중", "진행").any(value::contains) -> GameStatus.IN_PROGRESS
        else -> GameStatus.SCHEDULED
    }
}

data class GameDto(
    val gameId: String,
    val gameDate: LocalDate,
    val homeTeamCode: String,
    val awayTeamCode: String,
    val startTime: String?,
    val stadium: String?,
    val status: GameStatus,
)

/**
 * KBO 경기 일정(GetSchedule) 응답 파서.
 *
 * 가정하는 JSON 구조 (루트가 배열이어도 동작):
 * {
 *   "games": [
 *     { "gameDate": "20240601", "awayTeamName": "LG", "homeTeamName": "두산",
 *       "gameTime": "18:30", "stadium": "잠실", "statusCode": "BEFORE" }
 *   ]
 * }
 */
@Component
class ScheduleParser(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parseSchedule(json: String): List<GameDto> {
        val root = objectMapper.parseTree(json)
        val rows = root.firstArrayOf("games", "gameList", "scheduleList", "rows", "list")
        return rows.mapNotNull { node ->
            runCatching { toGameDto(node) }.getOrElse { error ->
                log.warn("경기 일정 행 파싱 실패: {}", error.message)
                null
            }
        }
    }

    private fun toGameDto(node: JsonNode): GameDto {
        val gameDate = parseGameDate(node.textOrNull("gameDate", "gdate", "g_dt", "date"))
        val awayCode = toTeamCode(node.text("awayTeamName", "awayTeam", "away", "visitTeam"))
        val homeCode = toTeamCode(node.text("homeTeamName", "homeTeam", "home"))
        if (awayCode.isEmpty() || homeCode.isEmpty()) {
            throw CrawlerException("경기의 팀 정보를 찾을 수 없습니다")
        }
        return GameDto(
            gameId = buildGameId(gameDate, awayCode, homeCode),
            gameDate = gameDate,
            homeTeamCode = homeCode,
            awayTeamCode = awayCode,
            startTime = node.textOrNull("gameTime", "startTime", "time"),
            stadium = node.textOrNull("stadium", "ballpark", "place"),
            status = toGameStatus(node.textOrNull("statusCode", "status", "gameStatus")),
        )
    }
}
