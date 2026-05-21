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
 * KBO 스코어보드(GetScoreBoardScroll) 응답 파서.
 *
 * 가정하는 JSON 구조:
 * {
 *   "gameId": "...",
 *   "home": { "innings": [0,1,0,2], "r": 5, "h": 9, "e": 0, "b": 3 },
 *   "away": { "innings": [1,0,0,0], "r": 1, "h": 6, "e": 1, "b": 2 }
 * }
 */
@Component
class ScoreParser(
    private val objectMapper: ObjectMapper,
) {
    fun parseScore(json: String): GameScoreDto {
        val root = objectMapper.parseTree(json)
        val home = root.teamNode("home", "homeTeam", "hScore")
        val away = root.teamNode("away", "awayTeam", "aScore")
        if (home.isMissingNode && away.isMissingNode) {
            throw CrawlerException("스코어보드 응답에서 점수 데이터를 찾을 수 없습니다")
        }

        val homeInnings = inningRuns(home)
        val awayInnings = inningRuns(away)
        val innings = (1..maxOf(homeInnings.size, awayInnings.size)).map { index ->
            InningScoreDto(
                inning = index,
                homeRuns = homeInnings.getOrElse(index - 1) { 0 },
                awayRuns = awayInnings.getOrElse(index - 1) { 0 },
            )
        }

        return GameScoreDto(
            gameId = root.text("gameId", "g_id", "gameSc"),
            innings = innings,
            homeR = home.intOf("r", "run", "runs", "score"),
            homeH = home.intOf("h", "hit", "hits"),
            homeE = home.intOf("e", "err", "error", "errors"),
            homeB = home.intOf("b", "bb", "walk", "walks"),
            awayR = away.intOf("r", "run", "runs", "score"),
            awayH = away.intOf("h", "hit", "hits"),
            awayE = away.intOf("e", "err", "error", "errors"),
            awayB = away.intOf("b", "bb", "walk", "walks"),
        )
    }

    private fun JsonNode.teamNode(vararg fieldNames: String): JsonNode {
        for (name in fieldNames) {
            val node = path(name)
            if (node.isObject) return node
        }
        return path(fieldNames.first())
    }

    private fun inningRuns(teamNode: JsonNode): List<Int> =
        teamNode.firstArrayOf("innings", "inning", "scores", "byInning").map { node ->
            when {
                node.isNumber -> node.asInt()
                node.isObject -> node.intOf("run", "runs", "score", "r")
                else -> node.asText().trim().toIntOrNull() ?: 0
            }
        }
}
