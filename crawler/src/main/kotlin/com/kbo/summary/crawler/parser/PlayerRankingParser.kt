package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

data class PlayerRankingDto(
    val rank: Int,
    val playerId: String,
    val playerName: String,
    val teamCode: String,
    val value: String,
)

/**
 * KBO 타자/투수 순위(POST /ws/Record.asmx/) 응답 파서.
 *
 * 순위 기준 지표(타율·홈런·ERA 등)는 종류가 달라 value 를 문자열로 보존한다.
 */
@Component
class PlayerRankingParser(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parseHitterRanking(json: String): List<PlayerRankingDto> = parseRanking(json)

    fun parsePitcherRanking(json: String): List<PlayerRankingDto> = parseRanking(json)

    private fun parseRanking(json: String): List<PlayerRankingDto> {
        val root = objectMapper.parseTree(json)
        val rows = root.firstArrayOf("rows", "ranks", "rankList", "list", "data")
        return rows.mapNotNull { node ->
            runCatching { toRankingDto(node) }.getOrElse { error ->
                log.warn("선수 순위 행 파싱 실패: {}", error.message)
                null
            }
        }
    }

    private fun toRankingDto(node: JsonNode): PlayerRankingDto {
        val playerId = node.textOrNull("playerId", "pcode", "playerCode")
            ?: throw IllegalArgumentException("playerId 누락")
        return PlayerRankingDto(
            rank = node.intOf("rank", "ranking", "rk"),
            playerId = playerId,
            playerName = node.text("playerName", "name"),
            teamCode = toTeamCode(node.text("teamCode", "teamName", "team")),
            value = node.text("value", "record", "stat", "val"),
        )
    }
}
