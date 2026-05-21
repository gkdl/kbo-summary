package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component

private val PLAYER_ID_REGEX = Regex("""playerId=(\d+)""", RegexOption.IGNORE_CASE)
private val DIGITS_REGEX = Regex("""(\d{4,})""")
private val TEAM_CODE_REGEX = Regex("""teamCode=([A-Za-z]{2,3})""")
private val POSITION_GROUPS = listOf("투수", "포수", "내야수", "외야수")

internal fun extractPlayerId(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    PLAYER_ID_REGEX.find(raw)?.let { return it.groupValues[1] }
    return DIGITS_REGEX.find(raw)?.groupValues?.get(1)
}

internal fun String?.toIntSafely(): Int =
    this?.replace(",", "")?.trim()?.toIntOrNull() ?: 0

// label 텍스트만 가진 말단 요소를 찾아 다음 형제 요소의 텍스트를 반환한다
internal fun Element.valueByLabel(vararg labels: String): String? {
    for (label in labels) {
        val labelElement = select("*").firstOrNull {
            it.children().isEmpty() && it.ownText().trim() == label
        }
        labelElement?.nextElementSibling()?.text()?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    }
    return null
}

internal fun Element.headerLabels(): List<String> =
    (selectFirst("thead tr") ?: select("tr").firstOrNull { it.selectFirst("th") != null })
        ?.select("th, td")
        ?.map { it.text().trim() }
        ?: emptyList()

// 테이블을 헤더명 → 셀값 Map 의 행 목록으로 변환한다
internal fun Element.dataRows(): List<Map<String, String>> {
    val headers = headerLabels()
    if (headers.isEmpty()) return emptyList()
    return select("tbody tr").ifEmpty { select("tr") }.mapNotNull { row ->
        val cells = row.select("td")
        if (cells.isEmpty()) {
            null
        } else {
            headers.indices.associate { i ->
                headers[i] to cells.getOrNull(i)?.text()?.trim().orEmpty()
            }
        }
    }
}

internal fun Map<String, String>.cell(vararg keys: String): String? {
    for (key in keys) {
        entries.firstOrNull { it.key.equals(key, ignoreCase = true) || it.key.contains(key) }
            ?.value?.takeIf { it.isNotBlank() }
            ?.let { return it }
    }
    return null
}

data class RosterEntryDto(
    val playerId: String,
    val name: String,
    val backNumber: String?,
    val positionGroup: String,
)

data class TeamRosterDto(
    val teamCode: String?,
    val players: List<RosterEntryDto>,
)

/**
 * KBO 팀 등록 선수(Register.aspx) HTML 파서.
 *
 * URL: /Player/Register.aspx?teamCode={code}
 * 가정: 포지션 그룹(투수/포수/내야수/외야수)별 <table>이 있고, 각 행에
 *       playerId를 담은 <a> 링크와 이름·등번호 셀이 존재한다.
 */
@Component
class TeamRosterParser {

    fun parseRoster(html: String): TeamRosterDto {
        val document = Jsoup.parse(html)
        val players = document.select("table").flatMap { table ->
            val group = positionGroupOf(table)
            table.select("tbody tr").ifEmpty { table.select("tr") }
                .mapNotNull { row -> toRosterEntry(row, group) }
        }
        return TeamRosterDto(
            teamCode = TEAM_CODE_REGEX.find(html)?.groupValues?.get(1)?.uppercase(),
            players = players.distinctBy { it.playerId },
        )
    }

    private fun toRosterEntry(row: Element, positionGroup: String): RosterEntryDto? {
        val link = row.select("a[href]").firstOrNull { extractPlayerId(it.attr("href")) != null }
            ?: return null
        val playerId = extractPlayerId(link.attr("href")) ?: return null
        val name = link.text().trim()
        if (name.isEmpty()) return null
        val backNumber = row.select("td").map { it.text().trim() }
            .firstOrNull { it.matches(Regex("""\d{1,3}""")) }
        return RosterEntryDto(playerId, name, backNumber, positionGroup)
    }

    private fun positionGroupOf(table: Element): String {
        val context = buildString {
            append(table.previousElementSibling()?.text().orEmpty())
            append(' ')
            append(table.parents().take(3).joinToString(" ") { "${it.id()} ${it.className()}" })
        }
        return POSITION_GROUPS.firstOrNull { context.contains(it) }.orEmpty()
    }
}
