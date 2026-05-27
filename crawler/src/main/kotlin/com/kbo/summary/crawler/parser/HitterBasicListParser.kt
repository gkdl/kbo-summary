package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

data class HitterStatRow(
    val playerId: String,
    val playerName: String,
    val rank: Int,
    val teamCode: String,
    val avg: BigDecimal?,
    val games: Int,
    val ab: Int,
    val hits: Int,
    val doubles: Int,
    val triples: Int,
    val hr: Int,
    val rbi: Int,
    val runs: Int,
    val bb: Int,
    val so: Int,
    val ops: BigDecimal?,
)

/**
 * 타자 시즌 기록 파서.
 * - Basic1 (GET /Record/Player/HitterBasic/Basic1.aspx): AVG, G, PA, AB, R, H, 2B, 3B, HR, TB, RBI, SAC, SF
 * - Basic2 (GET /Record/Player/HitterBasic/Basic2.aspx): AVG, BB, IBB, HBP, SO, GDP, SLG, OBP, OPS, MH, RISP
 *
 * 두 페이지를 playerId 로 머지해 한 List<HitterStatRow> 로 반환한다.
 * SB(도루) 는 두 페이지 모두에 없으므로 0 으로 둔다 (필요 시 Runner 페이지 추가).
 */
@Component
class HitterBasicListParser {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parse(basic1Html: String, basic2Html: String): List<HitterStatRow> {
        val basic1 = parseBasic1(basic1Html).associateBy { it.playerId }
        val basic2 = parseBasic2(basic2Html).associateBy { it.playerId }
        return basic1.values.map { a ->
            val b = basic2[a.playerId]
            a.copy(
                playerName = a.playerName.ifEmpty { b?.playerName.orEmpty() },
                bb = b?.bb ?: 0,
                so = b?.so ?: 0,
                ops = b?.ops,
            )
        }
    }

    private fun parseBasic1(html: String): List<HitterStatRow> =
        parseRows(html) { rank, playerId, playerName, team, cells ->
            HitterStatRow(
                playerId = playerId,
                playerName = playerName,
                rank = rank,
                teamCode = toTeamCode(team),
                avg = cells["HRA_RT"].toDecimalOrNull(),
                games = cells["GAME_CN"].toIntOrZero(),
                ab = cells["AB_CN"].toIntOrZero(),
                hits = cells["HIT_CN"].toIntOrZero(),
                doubles = cells["H2_CN"].toIntOrZero(),
                triples = cells["H3_CN"].toIntOrZero(),
                hr = cells["HR_CN"].toIntOrZero(),
                rbi = cells["RBI_CN"].toIntOrZero(),
                runs = cells["RUN_CN"].toIntOrZero(),
                bb = 0,
                so = 0,
                ops = null,
            )
        }

    private fun parseBasic2(html: String): List<HitterStatRow> =
        parseRows(html) { rank, playerId, playerName, team, cells ->
            HitterStatRow(
                playerId = playerId,
                playerName = playerName,
                rank = rank,
                teamCode = toTeamCode(team),
                avg = cells["HRA_RT"].toDecimalOrNull(),
                games = 0,
                ab = 0,
                hits = 0,
                doubles = 0,
                triples = 0,
                hr = 0,
                rbi = 0,
                runs = 0,
                bb = cells["BB_CN"].toIntOrZero(),
                so = cells["KK_CN"].toIntOrZero(),
                ops = cells["OPS_RT"].toDecimalOrNull(),
            )
        }

    private fun parseRows(
        html: String,
        toRow: (rank: Int, playerId: String, playerName: String, team: String, cells: Map<String, String>) -> HitterStatRow,
    ): List<HitterStatRow> {
        val doc = Jsoup.parse(html)
        val table = doc.selectFirst("table.tData01.tt") ?: run {
            log.warn("타자 통계 테이블(table.tData01.tt) 을 찾지 못함")
            return emptyList()
        }
        return table.select("tbody > tr").mapNotNull { tr ->
            runCatching { rowFrom(tr, toRow) }
                .onFailure { log.warn("타자 행 파싱 실패: {}", it.message) }
                .getOrNull()
        }
    }

    private fun rowFrom(
        tr: Element,
        toRow: (rank: Int, playerId: String, playerName: String, team: String, cells: Map<String, String>) -> HitterStatRow,
    ): HitterStatRow? {
        val tds = tr.select("> td")
        if (tds.size < 3) return null
        val rank = tds[0].text().trim().toIntOrNull() ?: return null
        val link = tds[1].selectFirst("a[href*=playerId=]") ?: return null
        val playerId = link.attr("href").substringAfter("playerId=").substringBefore("&")
            .takeIf { it.isNotEmpty() } ?: return null
        val playerName = link.text().trim()
        val team = tds[2].text().trim()
        val cells = tds.drop(3).associate { (it.attr("data-id") ?: "") to it.text().trim() }
        return toRow(rank, playerId, playerName, team, cells)
    }
}
