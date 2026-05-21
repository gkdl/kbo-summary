package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

data class HitterGameLogDto(
    val gameDate: String?,
    val opponent: String?,
    val atBats: Int,
    val runs: Int,
    val hits: Int,
    val rbi: Int,
    val homeRuns: Int,
    val walks: Int,
    val strikeOuts: Int,
)

data class PitcherGameLogDto(
    val gameDate: String?,
    val opponent: String?,
    val result: String?,
    val inningsPitched: String?,
    val hits: Int,
    val runs: Int,
    val earnedRuns: Int,
    val walks: Int,
    val strikeOuts: Int,
)

/**
 * KBO 선수 경기별 기록(HitterDetail/PitcherDetail GameDay.aspx) HTML 파서.
 *
 * URL: /Record/Player/{HitterDetail|PitcherDetail}/GameDay.aspx?playerId={id}
 * 가정: 경기 기록이 헤더 행을 가진 <table>로 제공된다. 최근 10경기만 반환한다.
 */
@Component
class PlayerGameLogParser {

    fun parseHitterGameLog(html: String): List<HitterGameLogDto> =
        gameRows(html).take(RECENT_GAME_LIMIT).map { row ->
            HitterGameLogDto(
                gameDate = row.cell("일자", "날짜", "경기일"),
                opponent = row.cell("상대", "상대팀"),
                atBats = row.cell("타수", "AB").toIntSafely(),
                runs = row.cell("득점", "R").toIntSafely(),
                hits = row.cell("안타", "H").toIntSafely(),
                rbi = row.cell("타점", "RBI").toIntSafely(),
                homeRuns = row.cell("홈런", "HR").toIntSafely(),
                walks = row.cell("볼넷", "BB").toIntSafely(),
                strikeOuts = row.cell("삼진", "SO", "K").toIntSafely(),
            )
        }

    fun parsePitcherGameLog(html: String): List<PitcherGameLogDto> =
        gameRows(html).take(RECENT_GAME_LIMIT).map { row ->
            PitcherGameLogDto(
                gameDate = row.cell("일자", "날짜", "경기일"),
                opponent = row.cell("상대", "상대팀"),
                result = row.cell("결과", "승패"),
                inningsPitched = row.cell("이닝", "IP"),
                hits = row.cell("피안타", "안타", "H").toIntSafely(),
                runs = row.cell("실점", "R").toIntSafely(),
                earnedRuns = row.cell("자책", "자책점", "ER").toIntSafely(),
                walks = row.cell("볼넷", "BB").toIntSafely(),
                strikeOuts = row.cell("삼진", "SO", "K").toIntSafely(),
            )
        }

    // 문서 내에서 가장 행이 많은 테이블을 경기 기록 테이블로 본다
    private fun gameRows(html: String): List<Map<String, String>> {
        val table = Jsoup.parse(html).select("table")
            .maxByOrNull { it.select("tr").size }
            ?: return emptyList()
        return table.dataRows()
    }

    private companion object {
        const val RECENT_GAME_LIMIT = 10
    }
}
