package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

data class StandingRow(
    val season: Int,
    val teamCode: String,
    val rank: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val winRate: BigDecimal?,
    val gamesBehind: BigDecimal?,
)

/**
 * 팀 순위 파서 — GET /Record/TeamRank/TeamRankDaily.aspx
 *
 * 페이지에는 두 개의 `table.tData` 가 있는데 첫 번째가 순위표, 두 번째가 팀간 승패표.
 * 컬럼: 순위, 팀명, 경기, 승, 패, 무, 승률, 게임차, 최근10경기, 연속, 홈, 방문
 *
 * 시즌은 페이지에 명시되지 않아 현재 연도를 사용한다.
 */
@Component
class TeamRankParser {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parse(html: String): List<StandingRow> {
        val doc = Jsoup.parse(html)
        val table = doc.select("table.tData").firstOrNull() ?: run {
            log.warn("팀 순위 테이블(table.tData) 을 찾지 못함")
            return emptyList()
        }
        val season = LocalDate.now().year
        return table.select("tbody > tr").mapNotNull { tr ->
            runCatching {
                val tds = tr.select("> td")
                if (tds.size < 8) return@runCatching null
                val rank = tds[0].text().trim().toIntOrNull() ?: return@runCatching null
                val team = tds[1].text().trim()
                val teamCode = toTeamCode(team)
                val gamesBehindText = tds[7].text().trim()
                StandingRow(
                    season = season,
                    teamCode = teamCode,
                    rank = rank,
                    wins = tds[3].text().toIntOrZero(),
                    losses = tds[4].text().toIntOrZero(),
                    draws = tds[5].text().toIntOrZero(),
                    winRate = tds[6].text().toDecimalOrNull(),
                    // "0" 또는 "-" 또는 "1.5" 형태. "0"/"-"은 0.0 으로 정규화.
                    gamesBehind = if (gamesBehindText == "0" || gamesBehindText == "-") {
                        BigDecimal.ZERO
                    } else {
                        gamesBehindText.toDecimalOrNull()
                    },
                )
            }.onFailure { log.warn("팀 순위 행 파싱 실패: {}", it.message) }.getOrNull()
        }
    }
}
