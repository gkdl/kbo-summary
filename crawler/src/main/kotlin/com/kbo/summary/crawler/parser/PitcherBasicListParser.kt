package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

data class PitcherStatRow(
    val playerId: String,
    val playerName: String,
    val rank: Int,
    val teamCode: String,
    val era: BigDecimal?,
    val games: Int,
    val wins: Int,
    val losses: Int,
    val saves: Int,
    val holds: Int,
    val ip: BigDecimal?,
    val hits: Int,
    val so: Int,
    val bb: Int,
    val whip: BigDecimal?,
)

/**
 * 투수 시즌 기록 파서.
 * GET /Record/Player/PitcherBasic/Basic1.aspx — PitcherStat 의 모든 필드를 한 페이지에서 얻을 수 있다.
 *
 * 컬럼: 순위, 선수명, 팀명, ERA, G, W, L, SV, HLD, WPCT, IP, H, HR, BB, HBP, SO, R, ER, WHIP
 */
@Component
class PitcherBasicListParser {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parse(html: String): List<PitcherStatRow> {
        val doc = Jsoup.parse(html)
        val table = doc.selectFirst("table.tData01.tt") ?: run {
            log.warn("투수 통계 테이블(table.tData01.tt) 을 찾지 못함")
            return emptyList()
        }
        return table.select("tbody > tr").mapNotNull { tr ->
            runCatching {
                val tds = tr.select("> td")
                if (tds.size < 3) return@runCatching null
                val rank = tds[0].text().trim().toIntOrNull() ?: return@runCatching null
                val link = tds[1].selectFirst("a[href*=playerId=]") ?: return@runCatching null
                val playerId = link.attr("href").substringAfter("playerId=").substringBefore("&")
                    .takeIf { it.isNotEmpty() } ?: return@runCatching null
                val playerName = link.text().trim()
                val team = tds[2].text().trim()
                val cells = tds.drop(3).associate { (it.attr("data-id") ?: "") to it.text().trim() }
                PitcherStatRow(
                    playerId = playerId,
                    playerName = playerName,
                    rank = rank,
                    teamCode = toTeamCode(team),
                    era = cells["ERA_RT"].toDecimalOrNull(),
                    games = cells["GAME_CN"].toIntOrZero(),
                    wins = cells["W_CN"].toIntOrZero(),
                    losses = cells["L_CN"].toIntOrZero(),
                    saves = cells["SV_CN"].toIntOrZero(),
                    holds = cells["HOLD_CN"].toIntOrZero(),
                    ip = parseInnings(cells["INN2_CN"]),
                    hits = cells["HIT_CN"].toIntOrZero(),
                    so = cells["KK_CN"].toIntOrZero(),
                    bb = cells["BB_CN"].toIntOrZero(),
                    whip = cells["WHIP_RT"].toDecimalOrNull(),
                )
            }.onFailure { log.warn("투수 행 파싱 실패: {}", it.message) }.getOrNull()
        }
    }
}
