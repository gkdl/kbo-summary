package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

data class TeamPitcherStatRow(
    val rank: Int,
    val teamCode: String,
    val era: BigDecimal?,
    val games: Int,
    val wins: Int,
    val losses: Int,
    val saves: Int,
    val holds: Int,
    val whip: BigDecimal?,
)

/**
 * 팀 투수 통계 파서 — GET /Record/Team/Pitcher/Basic1.aspx
 *
 * 영속화 엔티티가 아직 없어 DTO 반환만 한다. 컬럼: 순위, 팀명, ERA, G, W, L, SV, HLD, WPCT, IP, H, HR, BB, HBP, SO, R, ER, WHIP
 */
@Component
class TeamPitcherParser {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parse(html: String): List<TeamPitcherStatRow> {
        val doc = Jsoup.parse(html)
        val table = doc.selectFirst("table.tData") ?: run {
            log.warn("팀 투수 통계 테이블을 찾지 못함")
            return emptyList()
        }
        return table.select("tbody > tr").mapNotNull { tr ->
            runCatching {
                val tds = tr.select("> td")
                if (tds.size < 3) return@runCatching null
                val rank = tds[0].text().trim().toIntOrNull() ?: return@runCatching null
                val team = tds[1].text().trim()
                val cells = tds.drop(2).associate { (it.attr("data-id") ?: "") to it.text().trim() }
                TeamPitcherStatRow(
                    rank = rank,
                    teamCode = toTeamCode(team),
                    era = cells["ERA_RT"].toDecimalOrNull(),
                    games = cells["GAME_CN"].toIntOrZero(),
                    wins = cells["W_CN"].toIntOrZero(),
                    losses = cells["L_CN"].toIntOrZero(),
                    saves = cells["SV_CN"].toIntOrZero(),
                    holds = cells["HOLD_CN"].toIntOrZero(),
                    whip = cells["WHIP_RT"].toDecimalOrNull(),
                )
            }.onFailure { log.warn("팀 투수 행 파싱 실패: {}", it.message) }.getOrNull()
        }
    }
}
