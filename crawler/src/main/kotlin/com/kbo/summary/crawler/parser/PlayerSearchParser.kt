package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component

data class PlayerSearchResultDto(
    val playerId: String,
    val name: String,
    val teamCode: String?,
    val teamName: String?,
    val position: String?,
)

/**
 * KBO 선수 검색(Search.aspx) HTML 파서.
 *
 * URL: /Player/Search.aspx?searchWord={keyword}
 * 가정: 검색 결과가 표 또는 목록이며, 각 항목에 playerId 링크가 존재한다.
 */
@Component
class PlayerSearchParser {

    fun parseSearch(html: String): List<PlayerSearchResultDto> {
        val document = Jsoup.parse(html)
        return document.select("table tbody tr, table tr, ul li")
            .mapNotNull { toSearchResult(it) }
            .distinctBy { it.playerId }
    }

    private fun toSearchResult(row: Element): PlayerSearchResultDto? {
        val link = row.select("a[href]").firstOrNull { extractPlayerId(it.attr("href")) != null }
            ?: return null
        val playerId = extractPlayerId(link.attr("href")) ?: return null
        val name = link.text().trim()
        if (name.isEmpty()) return null

        val cells = row.select("td").map { it.text().trim() }.filter { it.isNotEmpty() }
        val teamName = cells.firstOrNull { KBO_TEAM_CODES.containsKey(it) }
        val position = cells.firstOrNull { it in POSITIONS }

        return PlayerSearchResultDto(
            playerId = playerId,
            name = name,
            teamCode = teamName?.let { toTeamCode(it) },
            teamName = teamName,
            position = position,
        )
    }

    private companion object {
        val POSITIONS = setOf("투수", "포수", "내야수", "외야수", "지명타자")
    }
}
