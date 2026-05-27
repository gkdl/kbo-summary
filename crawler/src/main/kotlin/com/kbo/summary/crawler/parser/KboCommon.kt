package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kbo.summary.core.exception.CrawlerException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 팀명/약칭 → KBO 2자리 팀 코드 (gameId·DB 표준). KIA=HT, 한화=HH 로 분리.
val KBO_TEAM_CODES: Map<String, String> = mapOf(
    "LG" to "LG",
    "KT" to "KT",
    "SSG" to "SK",
    "NC" to "NC",
    "두산" to "OB",
    "KIA" to "HT",
    "기아" to "HT",
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

// gameId = YYYYMMDD + 어웨이코드 + 홈코드 + "0"
internal fun buildGameId(gameDate: LocalDate, awayTeamCode: String, homeTeamCode: String): String =
    gameDate.format(DateTimeFormatter.BASIC_ISO_DATE) + awayTeamCode + homeTeamCode + "0"

// "0.371", "27" 같은 셀 문자열을 BigDecimal/Int 로 변환 — '-' 또는 빈 문자열은 null/0
internal fun String?.toDecimalOrNull(): BigDecimal? {
    val trimmed = this?.trim()?.takeIf { it.isNotEmpty() && it != "-" } ?: return null
    return runCatching { BigDecimal(trimmed.replace(",", "")) }.getOrNull()
}

internal fun String?.toIntOrZero(): Int {
    val trimmed = this?.trim()?.takeIf { it.isNotEmpty() && it != "-" } ?: return 0
    return trimmed.replace(",", "").toIntOrNull() ?: 0
}

// KBO 이닝 표기 "63 2/3", "12 1/3", "5" 를 BigDecimal 로. 1/3=0.33, 2/3=0.67.
internal fun parseInnings(text: String?): BigDecimal? {
    val raw = text?.trim()?.takeIf { it.isNotEmpty() && it != "-" } ?: return null
    var sum = BigDecimal.ZERO
    var matched = false
    raw.split(" ", " ").filter { it.isNotEmpty() }.forEach { part ->
        when (part) {
            "1/3" -> { sum = sum.add(BigDecimal("0.33")); matched = true }
            "2/3" -> { sum = sum.add(BigDecimal("0.67")); matched = true }
            else -> part.replace(",", "").toBigDecimalOrNull()?.let {
                sum = sum.add(it); matched = true
            }
        }
    }
    return if (matched) sum else null
}
