package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kbo.summary.core.exception.CrawlerException
import org.springframework.stereotype.Component

data class InningScoreDto(
    val inning: Int,
    val homeRuns: Int,
    val awayRuns: Int,
)

data class GameScoreDto(
    val gameId: String,
    val innings: List<InningScoreDto>,
    val homeR: Int,
    val homeH: Int,
    val homeE: Int,
    val homeB: Int,
    val awayR: Int,
    val awayH: Int,
    val awayE: Int,
    val awayB: Int,
)

/**
 * KBO 스코어보드(POST /ws/Schedule.asmx/GetScoreBoardScroll) 응답 파서.
 *
 * 실제 응답:
 * { "G_ID": "...", "code": "100",
 *   "table2": "<이닝별 JSON 문자열>", "table3": "<R/H/E/B JSON 문자열>" }
 * - table2/table3 은 문자열로 인코딩된 중첩 JSON → 다시 파싱한다
 * - rows[0] = 어웨이, rows[1] = 홈. 셀의 "-" 는 진행하지 않은 이닝.
 */
@Component
class ScoreParser(
    private val objectMapper: ObjectMapper,
) {
    fun parseScore(json: String): GameScoreDto {
        val root = objectMapper.parseTree(json)
        if (root.path("code").asText() != SUCCESS_CODE) {
            throw CrawlerException("스코어보드 응답 실패: ${root.path("msg").asText()}")
        }

        val inningTable = objectMapper.parseTree(root.path("table2").asText())
        val awayInnings = cellInts(inningTable, AWAY_ROW)
        val homeInnings = cellInts(inningTable, HOME_ROW)
        val innings = (0 until maxOf(awayInnings.size, homeInnings.size)).mapNotNull { index ->
            val away = awayInnings.getOrNull(index)
            val home = homeInnings.getOrNull(index)
            if (away == null && home == null) {
                null
            } else {
                InningScoreDto(inning = index + 1, homeRuns = home ?: 0, awayRuns = away ?: 0)
            }
        }

        val rhebTable = objectMapper.parseTree(root.path("table3").asText())
        val away = rheb(rhebTable, AWAY_ROW)
        val home = rheb(rhebTable, HOME_ROW)

        return GameScoreDto(
            gameId = root.path("G_ID").asText(),
            innings = innings,
            homeR = home[0],
            homeH = home[1],
            homeE = home[2],
            homeB = home[3],
            awayR = away[0],
            awayH = away[1],
            awayE = away[2],
            awayB = away[3],
        )
    }

    // 이닝별 셀 → Int? 목록 ("-" 등 숫자가 아니면 null = 미진행 이닝)
    private fun cellInts(table: JsonNode, rowIndex: Int): List<Int?> {
        val row = table.path("rows").path(rowIndex).path("row")
        if (!row.isArray) return emptyList()
        return row.map { it.path("Text").asText().trim().toIntOrNull() }
    }

    // R/H/E/B 4개 값 (부족하면 0으로 채움)
    private fun rheb(table: JsonNode, rowIndex: Int): List<Int> {
        val row = table.path("rows").path(rowIndex).path("row")
        val values = if (row.isArray) {
            row.map { it.path("Text").asText().trim().toIntOrNull() ?: 0 }
        } else {
            emptyList()
        }
        return List(4) { values.getOrElse(it) { 0 } }
    }

    private companion object {
        const val SUCCESS_CODE = "100"
        const val AWAY_ROW = 0
        const val HOME_ROW = 1
    }
}
