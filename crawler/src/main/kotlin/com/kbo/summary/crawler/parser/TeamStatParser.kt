package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

internal fun JsonNode.firstRowOrSelf(): JsonNode =
    firstArrayOf("rows", "list", "data", "stats", "records").firstOrNull() ?: this

data class TeamHitterStatDto(
    val teamCode: String,
    val season: Int,
    val games: Int,
    val avg: BigDecimal?,
    val runs: Int,
    val hits: Int,
    val homeRuns: Int,
    val rbi: Int,
    val stolenBases: Int,
    val walks: Int,
    val strikeOuts: Int,
    val ops: BigDecimal?,
)

data class TeamPitcherStatDto(
    val teamCode: String,
    val season: Int,
    val era: BigDecimal?,
    val games: Int,
    val wins: Int,
    val losses: Int,
    val saves: Int,
    val holds: Int,
    val inningsPitched: String?,
    val hits: Int,
    val strikeOuts: Int,
    val walks: Int,
    val whip: BigDecimal?,
)

/**
 * KBO 팀 기록(POST /ws/Record.asmx/) 응답 파서.
 *
 * 응답에 여러 팀이 담겨도 첫 행(또는 단일 객체)을 기준으로 한 팀 DTO를 만든다.
 */
@Component
class TeamStatParser(
    private val objectMapper: ObjectMapper,
) {
    fun parseTeamHitterStat(json: String): TeamHitterStatDto {
        val node = objectMapper.parseTree(json).firstRowOrSelf()
        return TeamHitterStatDto(
            teamCode = toTeamCode(node.text("teamCode", "teamName", "team")),
            season = node.intOrNull("season", "year") ?: LocalDate.now().year,
            games = node.intOf("g", "games", "game"),
            avg = node.decimalOrNull("avg", "hra", "bra"),
            runs = node.intOf("r", "run", "runs"),
            hits = node.intOf("h", "hit", "hits"),
            homeRuns = node.intOf("hr", "homeRun"),
            rbi = node.intOf("rbi"),
            stolenBases = node.intOf("sb", "steal"),
            walks = node.intOf("bb", "walk", "walks"),
            strikeOuts = node.intOf("so", "kk", "strikeOut"),
            ops = node.decimalOrNull("ops"),
        )
    }

    fun parseTeamPitcherStat(json: String): TeamPitcherStatDto {
        val node = objectMapper.parseTree(json).firstRowOrSelf()
        return TeamPitcherStatDto(
            teamCode = toTeamCode(node.text("teamCode", "teamName", "team")),
            season = node.intOrNull("season", "year") ?: LocalDate.now().year,
            era = node.decimalOrNull("era"),
            games = node.intOf("g", "games", "game"),
            wins = node.intOf("w", "win", "wins"),
            losses = node.intOf("l", "lose", "losses"),
            saves = node.intOf("sv", "save", "saves"),
            holds = node.intOf("hold", "holds", "hld"),
            inningsPitched = node.textOrNull("ip", "inning", "inningsPitched"),
            hits = node.intOf("h", "hit", "hits"),
            strikeOuts = node.intOf("so", "kk", "strikeOut"),
            walks = node.intOf("bb", "walk", "walks"),
            whip = node.decimalOrNull("whip"),
        )
    }
}
