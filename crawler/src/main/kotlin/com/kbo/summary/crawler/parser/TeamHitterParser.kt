package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

data class TeamHitterStatRow(
    val rank: Int,
    val teamCode: String,
    val avg: BigDecimal?,
    val games: Int,
    val ab: Int,
    val hits: Int,
    val hr: Int,
    val rbi: Int,
)

/**
 * 팀 타격 통계 파서 — GET /Record/Team/Hitter/Basic1.aspx
 *
 * 영속화 엔티티가 아직 없어 DTO 반환만 한다. 컬럼: 순위, 팀명, AVG, G, PA, AB, R, H, 2B, 3B, HR, TB, RBI, SAC, SF
 */
@Component
class TeamHitterParser {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parse(html: String): List<TeamHitterStatRow> {
        val doc = Jsoup.parse(html)
        val table = doc.selectFirst("table.tData") ?: run {
            log.warn("팀 타격 통계 테이블을 찾지 못함")
            return emptyList()
        }
        return table.select("tbody > tr").mapNotNull { tr ->
            runCatching {
                val tds = tr.select("> td")
                if (tds.size < 3) return@runCatching null
                val rank = tds[0].text().trim().toIntOrNull() ?: return@runCatching null
                val team = tds[1].text().trim()
                val cells = tds.drop(2).associate { (it.attr("data-id") ?: "") to it.text().trim() }
                TeamHitterStatRow(
                    rank = rank,
                    teamCode = toTeamCode(team),
                    avg = cells["HRA_RT"].toDecimalOrNull(),
                    games = cells["GAME_CN"].toIntOrZero(),
                    ab = cells["AB_CN"].toIntOrZero(),
                    hits = cells["HIT_CN"].toIntOrZero(),
                    hr = cells["HR_CN"].toIntOrZero(),
                    rbi = cells["RBI_CN"].toIntOrZero(),
                )
            }.onFailure { log.warn("팀 타격 행 파싱 실패: {}", it.message) }.getOrNull()
        }
    }
}
