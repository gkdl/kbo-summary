package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component

data class PlayerProfileDto(
    val playerId: String?,
    val name: String,
    val teamName: String?,
    val teamCode: String?,
    val position: String?,
    val backNumber: String?,
    val bats: String?,
    val throws: String?,
    val throwsBats: String?,
    val birthDate: String?,
    val height: Int?,
    val weight: Int?,
    val school: String?,
    val debutYear: Int?,
    val careerStats: Map<String, String>,
)

/**
 * KBO 선수 기본정보(HitterDetail/PitcherDetail Basic.aspx) HTML 파서.
 *
 * URL: /Record/Player/{HitterDetail|PitcherDetail}/Basic.aspx?playerId={id}
 * 가정: 기본정보는 라벨/값 쌍으로, 통산기록은 헤더 + 통산 행을 가진 <table>로 표현된다.
 */
@Component
class PlayerProfileParser {

    fun parseHitterProfile(html: String): PlayerProfileDto = parseProfile(html)

    fun parsePitcherProfile(html: String): PlayerProfileDto = parseProfile(html)

    private fun parseProfile(html: String): PlayerProfileDto {
        val document = Jsoup.parse(html)
        val body = document.body()

        val throwsBats = body.valueByLabel("투타", "투타유형")
        val throwHand = THROW_REGEX.find(throwsBats.orEmpty())?.value
        val batHand = BAT_REGEX.find(throwsBats.orEmpty())?.value
        val heightWeight = body.valueByLabel("신장/체중", "신장체중", "체격")

        return PlayerProfileDto(
            playerId = extractPlayerId(html),
            name = body.valueByLabel("선수명", "이름", "성명")
                ?: document.title().substringBefore('|').trim(),
            teamName = body.valueByLabel("팀명", "소속팀", "팀"),
            teamCode = body.valueByLabel("팀명", "소속팀", "팀")?.let { toTeamCode(it) },
            position = body.valueByLabel("포지션"),
            backNumber = body.valueByLabel("등번호", "배번"),
            bats = batHand,
            throws = throwHand,
            throwsBats = throwsBats,
            birthDate = body.valueByLabel("생년월일", "생일"),
            height = nthNumber(heightWeight, 0) ?: nthNumber(body.valueByLabel("신장", "키"), 0),
            weight = nthNumber(heightWeight, 1) ?: nthNumber(body.valueByLabel("체중", "몸무게"), 0),
            school = body.valueByLabel("출신교", "출신학교", "학교"),
            debutYear = body.valueByLabel("입단년도", "데뷔년도", "데뷔")
                ?.let { YEAR_REGEX.find(it)?.value?.toIntOrNull() },
            careerStats = parseCareerStats(document),
        )
    }

    private fun parseCareerStats(document: Document): Map<String, String> {
        val table = document.select("table").firstOrNull { it.text().contains("통산") }
            ?: return emptyMap()
        val rows = table.dataRows()
        return rows.firstOrNull { row -> row.values.any { it.contains("통산") } }
            ?: rows.lastOrNull()
            ?: emptyMap()
    }

    private fun nthNumber(raw: String?, index: Int): Int? =
        raw?.let { value ->
            NUMBER_REGEX.findAll(value).map { it.value }.toList().getOrNull(index)?.toIntOrNull()
        }

    private companion object {
        val THROW_REGEX = Regex("[좌우양]투")
        val BAT_REGEX = Regex("[좌우양]타")
        val YEAR_REGEX = Regex("(19|20)\\d{2}")
        val NUMBER_REGEX = Regex("\\d+")
    }
}
