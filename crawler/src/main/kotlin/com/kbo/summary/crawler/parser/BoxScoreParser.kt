package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kbo.summary.core.exception.CrawlerException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

data class HitterRecordDto(
    val playerName: String,
    val battingOrder: Int?,
    val position: String?,
    val teamCode: String,
    val atBats: Int,
    val hits: Int,
    val rbi: Int,
    val runs: Int,
    val avg: BigDecimal?,
)

data class PitcherRecordDto(
    val playerName: String,
    val teamCode: String,
    val role: String?,
    val decision: String?,
    val wins: Int,
    val losses: Int,
    val saves: Int,
    val inningsPitched: BigDecimal?,
    val battersFaced: Int,
    val pitchCount: Int,
    val atBats: Int,
    val hits: Int,
    val homeRuns: Int,
    val walks: Int,
    val strikeOuts: Int,
    val runs: Int,
    val earnedRuns: Int,
    val era: BigDecimal?,
)

data class BoxScoreDto(
    val gameId: String,
    val awayHitters: List<HitterRecordDto>,
    val homeHitters: List<HitterRecordDto>,
    val awayPitchers: List<PitcherRecordDto>,
    val homePitchers: List<PitcherRecordDto>,
)

/**
 * KBO 박스스코어(POST /ws/Schedule.asmx/GetBoxScoreScroll) 응답 파서.
 *
 * 응답 형식:
 * { "arrHitter":  [{table1, table2, table3}, {table1, table2, table3}],   // [어웨이, 홈]
 *   "arrPitcher": [{table}, {table}],                                       // [어웨이, 홈]
 *   "code": "100" }
 *
 * - table1/table2/table3 은 JSON-string (이중 인코딩) — parseTree 로 다시 파싱
 * - 타자: table1 = [타순, 포지션, 이름]  /  table3 = [AB, H, RBI, R, AVG]
 *   같은 row index 가 같은 선수 (table2 = 이닝별 타석 결과, 사용 안 함)
 * - 투수: 단일 table 에 17 컬럼 (선수명/등판/결과/승/패/세/이닝/타자/투구수/타수/피안타/홈런/4사구/삼진/실점/자책/평균자책점)
 */
@Component
class BoxScoreParser(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parseBoxScore(gameId: String, json: String, awayTeamCode: String, homeTeamCode: String): BoxScoreDto {
        val root = objectMapper.parseTree(json)
        if (root.path("code").asText() != SUCCESS_CODE) {
            throw CrawlerException("박스스코어 응답 실패: ${root.path("msg").asText()}")
        }

        val arrHitter = root.path("arrHitter")
        val arrPitcher = root.path("arrPitcher")

        return BoxScoreDto(
            gameId = gameId,
            awayHitters = parseHitters(arrHitter.path(AWAY_INDEX), awayTeamCode),
            homeHitters = parseHitters(arrHitter.path(HOME_INDEX), homeTeamCode),
            awayPitchers = parsePitchers(arrPitcher.path(AWAY_INDEX), awayTeamCode),
            homePitchers = parsePitchers(arrPitcher.path(HOME_INDEX), homeTeamCode),
        )
    }

    private fun parseHitters(teamNode: JsonNode, teamCode: String): List<HitterRecordDto> {
        if (teamNode.isMissingNode || teamNode.isNull) return emptyList()
        val nameTable = decodeTable(teamNode.path("table1")) ?: return emptyList()
        val statTable = decodeTable(teamNode.path("table3")) ?: return emptyList()
        val nameRows = nameTable.path("rows")
        val statRows = statTable.path("rows")
        if (!nameRows.isArray || !statRows.isArray) return emptyList()
        val count = minOf(nameRows.size(), statRows.size())
        return (0 until count).mapNotNull { i ->
            runCatching { toHitter(nameRows[i], statRows[i], teamCode) }
                .onFailure { log.warn("타자 행 {} 파싱 실패: {}", i, it.message) }
                .getOrNull()
        }
    }

    private fun toHitter(nameRow: JsonNode, statRow: JsonNode, teamCode: String): HitterRecordDto? {
        val nameCells = cellTexts(nameRow)
        val statCells = cellTexts(statRow)
        if (nameCells.size < 3 || statCells.size < 5) return null
        val playerName = nameCells[2].trim()
        if (playerName.isEmpty()) return null
        return HitterRecordDto(
            playerName = playerName,
            battingOrder = nameCells[0].trim().toIntOrNull(),
            position = nameCells[1].trim().takeIf { it.isNotEmpty() },
            teamCode = teamCode,
            atBats = statCells[0].toIntOrZero(),
            hits = statCells[1].toIntOrZero(),
            rbi = statCells[2].toIntOrZero(),
            runs = statCells[3].toIntOrZero(),
            avg = statCells[4].toDecimalOrNull(),
        )
    }

    private fun parsePitchers(teamNode: JsonNode, teamCode: String): List<PitcherRecordDto> {
        if (teamNode.isMissingNode || teamNode.isNull) return emptyList()
        val table = decodeTable(teamNode.path("table")) ?: return emptyList()
        val rows = table.path("rows")
        if (!rows.isArray) return emptyList()
        return rows.mapNotNull { row ->
            runCatching { toPitcher(row, teamCode) }
                .onFailure { log.warn("투수 행 파싱 실패: {}", it.message) }
                .getOrNull()
        }
    }

    private fun toPitcher(row: JsonNode, teamCode: String): PitcherRecordDto? {
        val c = cellTexts(row)
        // 17컬럼: 0이름 1등판 2결과 3승 4패 5세 6이닝 7타자 8투구수 9타수 10피안타 11홈런 12사사구 13삼진 14실점 15자책 16ERA
        if (c.size < 17) return null
        val playerName = c[0].trim()
        if (playerName.isEmpty()) return null
        // 헤더: 0선수명 1등판 2결과 3승 4패 5세 6이닝 7타자 8투구수 9타수 10피안타 11홈런 12사사구 13삼진 14실점 15자책 16ERA
        return PitcherRecordDto(
            playerName = playerName,
            teamCode = teamCode,
            role = c[1].trim().takeIf { it.isNotEmpty() && it != "&nbsp;" },
            decision = c[2].trim().takeIf { it.isNotEmpty() && it != "&nbsp;" },
            wins = c[3].toIntOrZero(),
            losses = c[4].toIntOrZero(),
            saves = c[5].toIntOrZero(),
            inningsPitched = parseInnings(c[6]),
            battersFaced = c[7].toIntOrZero(),
            pitchCount = c[8].toIntOrZero(),
            atBats = c[9].toIntOrZero(),
            hits = c[10].toIntOrZero(),
            homeRuns = c[11].toIntOrZero(),
            walks = c[12].toIntOrZero(),
            strikeOuts = c[13].toIntOrZero(),
            runs = c[14].toIntOrZero(),
            earnedRuns = c[15].toIntOrZero(),
            era = c[16].toDecimalOrNull(),
        )
    }

    private fun cellTexts(row: JsonNode): List<String> {
        val cells = row.path("row")
        if (!cells.isArray) return emptyList()
        return cells.map { it.path("Text").asText("") }
    }

    // table1/table2/table3 셀은 JSON 문자열로 한 번 더 감싸여 있음
    private fun decodeTable(node: JsonNode): JsonNode? {
        if (node.isMissingNode || node.isNull) return null
        val raw = node.asText().takeIf { it.isNotEmpty() } ?: return null
        return runCatching { objectMapper.readTree(raw) }.getOrNull()
    }

    private companion object {
        const val SUCCESS_CODE = "100"
        const val AWAY_INDEX = 0
        const val HOME_INDEX = 1
    }
}
