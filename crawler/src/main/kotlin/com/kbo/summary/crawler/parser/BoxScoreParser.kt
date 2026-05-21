package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

data class HitterRecordDto(
    val playerId: String,
    val playerName: String,
    val teamCode: String,
    val battingOrder: Int?,
    val position: String?,
    val atBats: Int,
    val runs: Int,
    val hits: Int,
    val rbi: Int,
    val homeRuns: Int,
    val walks: Int,
    val strikeOuts: Int,
)

data class PitcherRecordDto(
    val playerId: String,
    val playerName: String,
    val teamCode: String,
    val inningsPitched: String?,
    val hits: Int,
    val runs: Int,
    val earnedRuns: Int,
    val walks: Int,
    val strikeOuts: Int,
    val decision: String?,
)

data class BoxScoreDto(
    val gameId: String,
    val hitters: List<HitterRecordDto>,
    val pitchers: List<PitcherRecordDto>,
    val winningPitcherId: String?,
    val losingPitcherId: String?,
    val savePitcherId: String?,
    val homeRunHitters: List<String>,
)

/**
 * KBO 박스스코어(GetBoxScoreScroll) 응답 파서.
 *
 * 가정하는 JSON 구조:
 * {
 *   "gameId": "...",
 *   "hitters":  [ { "playerId": "...", "playerName": "...", "teamCode": "두산",
 *                   "battingOrder": 1, "position": "CF",
 *                   "ab": 4, "r": 1, "h": 2, "rbi": 1, "hr": 0, "bb": 0, "so": 1 } ],
 *   "pitchers": [ { "playerId": "...", "playerName": "...", "teamCode": "LG",
 *                   "ip": "5.1", "h": 4, "r": 2, "er": 2, "bb": 1, "so": 6,
 *                   "decision": "WIN" } ],
 *   "homeRuns": [ "홍길동(1호)" ]
 * }
 */
@Component
class BoxScoreParser(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parseBoxScore(json: String): BoxScoreDto {
        val root = objectMapper.parseTree(json)

        val hitters = root.firstArrayOf("hitters", "batters", "hitterList")
            .mapNotNull { node -> parseRow("타자") { toHitter(node) } }
        val pitchers = root.firstArrayOf("pitchers", "pitcherList")
            .mapNotNull { node -> parseRow("투수") { toPitcher(node) } }

        return BoxScoreDto(
            gameId = root.text("gameId", "g_id"),
            hitters = hitters,
            pitchers = pitchers,
            winningPitcherId = root.textOrNull("winningPitcherId", "winPitcherId")
                ?: pitchers.firstOrNull { it.decision.equals("WIN", ignoreCase = true) }?.playerId,
            losingPitcherId = root.textOrNull("losingPitcherId", "losePitcherId")
                ?: pitchers.firstOrNull { it.decision.equals("LOSS", ignoreCase = true) }?.playerId,
            savePitcherId = root.textOrNull("savePitcherId", "savedPitcherId")
                ?: pitchers.firstOrNull { it.decision.equals("SAVE", ignoreCase = true) }?.playerId,
            homeRunHitters = root.firstArrayOf("homeRuns", "homerun", "hr")
                .map { it.asText().trim() }
                .filter { it.isNotEmpty() },
        )
    }

    private fun toHitter(node: JsonNode): HitterRecordDto {
        val playerId = node.textOrNull("playerId", "pcode", "playerCode")
            ?: throw IllegalArgumentException("playerId 누락")
        return HitterRecordDto(
            playerId = playerId,
            playerName = node.text("playerName", "name"),
            teamCode = toTeamCode(node.text("teamCode", "teamName", "team")),
            battingOrder = node.intOrNull("battingOrder", "batOrder", "seqno"),
            position = node.textOrNull("position", "pos"),
            atBats = node.intOf("ab", "atBats"),
            runs = node.intOf("r", "run", "runs"),
            hits = node.intOf("h", "hit", "hits"),
            rbi = node.intOf("rbi", "rbiCount"),
            homeRuns = node.intOf("hr", "homeRun"),
            walks = node.intOf("bb", "walk", "walks"),
            strikeOuts = node.intOf("so", "kk", "strikeOut"),
        )
    }

    private fun toPitcher(node: JsonNode): PitcherRecordDto {
        val playerId = node.textOrNull("playerId", "pcode", "playerCode")
            ?: throw IllegalArgumentException("playerId 누락")
        return PitcherRecordDto(
            playerId = playerId,
            playerName = node.text("playerName", "name"),
            teamCode = toTeamCode(node.text("teamCode", "teamName", "team")),
            inningsPitched = node.textOrNull("ip", "inning", "inningsPitched"),
            hits = node.intOf("h", "hit", "hits"),
            runs = node.intOf("r", "run", "runs"),
            earnedRuns = node.intOf("er", "earnedRun", "earnedRuns"),
            walks = node.intOf("bb", "walk", "walks"),
            strikeOuts = node.intOf("so", "kk", "strikeOut"),
            decision = node.textOrNull("decision", "wls", "result"),
        )
    }

    private fun <T> parseRow(label: String, block: () -> T): T? =
        runCatching { block() }.getOrElse {
            log.warn("박스스코어 {} 기록 파싱 실패: {}", label, it.message)
            null
        }
}
