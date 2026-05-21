package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

data class StandingDto(
    val season: Int,
    val rank: Int,
    val teamCode: String,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val winRate: BigDecimal?,
    val gamesBehind: BigDecimal?,
)

/**
 * KBO 팀 순위(GetStandings) 응답 파서.
 *
 * 가정하는 JSON 구조 (루트가 배열이어도 동작):
 * {
 *   "season": 2024,
 *   "standings": [
 *     { "rank": 1, "teamName": "KIA", "wins": 87, "losses": 55, "draws": 2,
 *       "winRate": "0.613", "gamesBehind": "0.0" }
 *   ]
 * }
 */
@Component
class StandingsParser(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parseStandings(json: String): List<StandingDto> {
        val root = objectMapper.parseTree(json)
        val defaultSeason = root.intOrNull("season", "year") ?: LocalDate.now().year
        val rows = root.firstArrayOf("standings", "rankList", "teamRanks", "rows", "list")
        return rows.mapNotNull { node ->
            runCatching { toStandingDto(node, defaultSeason) }.getOrElse { error ->
                log.warn("팀 순위 행 파싱 실패: {}", error.message)
                null
            }
        }
    }

    private fun toStandingDto(node: JsonNode, defaultSeason: Int): StandingDto {
        val teamCode = toTeamCode(node.text("teamCode", "teamName", "team"))
        if (teamCode.isEmpty()) {
            throw IllegalArgumentException("팀 정보 누락")
        }
        return StandingDto(
            season = node.intOrNull("season", "year") ?: defaultSeason,
            rank = node.intOf("rank", "ranking", "rk"),
            teamCode = teamCode,
            wins = node.intOf("wins", "win", "w"),
            losses = node.intOf("losses", "lose", "l"),
            draws = node.intOf("draws", "draw", "tie", "d"),
            winRate = node.decimalOrNull("winRate", "wra", "winningRate", "pct"),
            gamesBehind = node.decimalOrNull("gamesBehind", "gb", "gameBehind"),
        )
    }
}
