package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

data class HitterStatDto(
    val playerId: String,
    val season: Int,
    val avg: BigDecimal?,
    val games: Int,
    val atBats: Int,
    val hits: Int,
    val doubles: Int,
    val triples: Int,
    val homeRuns: Int,
    val rbi: Int,
    val runs: Int,
    val stolenBases: Int,
    val walks: Int,
    val strikeOuts: Int,
    val ops: BigDecimal?,
)

data class PitcherStatDto(
    val playerId: String,
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
 * KBO 선수 시즌 기록(POST /ws/Record.asmx/) 응답 파서.
 *
 * 응답이 여러 행이어도 첫 행(또는 단일 객체) 기준으로 한 선수 DTO를 만든다.
 */
@Component
class PlayerStatParser(
    private val objectMapper: ObjectMapper,
) {
    fun parseHitterStat(json: String): HitterStatDto {
        val node = objectMapper.parseTree(json).firstRowOrSelf()
        return HitterStatDto(
            playerId = node.text("playerId", "pcode", "playerCode"),
            season = node.intOrNull("season", "year") ?: LocalDate.now().year,
            avg = node.decimalOrNull("avg", "hra"),
            games = node.intOf("g", "games", "game"),
            atBats = node.intOf("ab", "atBats"),
            hits = node.intOf("h", "hit", "hits"),
            doubles = node.intOf("2b", "double", "doubles"),
            triples = node.intOf("3b", "triple", "triples"),
            homeRuns = node.intOf("hr", "homeRun"),
            rbi = node.intOf("rbi"),
            runs = node.intOf("r", "run", "runs"),
            stolenBases = node.intOf("sb", "steal"),
            walks = node.intOf("bb", "walk", "walks"),
            strikeOuts = node.intOf("so", "kk", "strikeOut"),
            ops = node.decimalOrNull("ops"),
        )
    }

    fun parsePitcherStat(json: String): PitcherStatDto {
        val node = objectMapper.parseTree(json).firstRowOrSelf()
        return PitcherStatDto(
            playerId = node.text("playerId", "pcode", "playerCode"),
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
