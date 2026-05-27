package com.kbo.summary.crawler.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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
 *
 * 실제 KBO 페이지는 ASP.NET WebForms 라 `[id$=playerProfile_lbl*]` 패턴을 사용한다.
 * 페이지 <title> 은 항상 "타자 | …" 또는 "투수 | …" 라 fallback 으로 쓰면 안 된다 (선수명이 아님).
 */
@Component
class PlayerProfileParser {

    fun parseHitterProfile(html: String): PlayerProfileDto = parseProfile(html)

    fun parsePitcherProfile(html: String): PlayerProfileDto = parseProfile(html)

    private fun parseProfile(html: String): PlayerProfileDto {
        val document = Jsoup.parse(html)
        val body = document.body()

        val name = body.lbl("lblName")
            ?: body.valueByLabel("선수명", "이름", "성명")
            ?: ""
        val backNumber = body.lbl("lblBackNo") ?: body.valueByLabel("등번호", "배번")
        val rawPosition = body.lbl("lblPosition") ?: body.valueByLabel("포지션")
        val birthDate = body.lbl("lblBirthday") ?: body.valueByLabel("생년월일", "생일")
        val heightWeight = body.lbl("lblHeightWeight")
            ?: body.valueByLabel("신장/체중", "신장체중", "체격")
        val school = body.lbl("lblCareer") ?: body.valueByLabel("출신교", "출신학교", "학교")

        // position 안 괄호에 (우투좌타) 같은 throwsBats 가 같이 있음 — "내야수(우투좌타)"
        val throwsBats = rawPosition?.let { extractInsideParens(it) }
            ?: body.valueByLabel("투타", "투타유형")
        val throwHand = THROW_REGEX.find(throwsBats.orEmpty())?.value
        val batHand = BAT_REGEX.find(throwsBats.orEmpty())?.value
        val position = rawPosition?.substringBefore('(')?.trim()?.takeIf { it.isNotEmpty() }

        val teamName = body.valueByLabel("팀명", "소속팀", "팀")

        return PlayerProfileDto(
            playerId = extractPlayerId(html),
            name = name,
            teamName = teamName,
            teamCode = teamName?.let { toTeamCode(it) },
            position = position,
            backNumber = backNumber,
            bats = batHand,
            throws = throwHand,
            throwsBats = throwsBats,
            birthDate = birthDate,
            height = nthNumber(heightWeight, 0),
            weight = nthNumber(heightWeight, 1),
            school = school,
            debutYear = parseDebutYear(body),
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

    // KBO 입단정보는 "23키움" 같은 2자리 연도라 (19|20) 패턴이 안 잡힘 — 2자리도 별도 처리
    private fun parseDebutYear(body: Element): Int? {
        val raw = body.lbl("lblJoinInfo")
            ?: body.valueByLabel("입단년도", "데뷔년도", "데뷔")
            ?: return null
        FULL_YEAR_REGEX.find(raw)?.value?.toIntOrNull()?.let { return it }
        // 2자리: "23키움" → 2023
        return SHORT_YEAR_REGEX.find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { 2000 + it }
    }

    private fun nthNumber(raw: String?, index: Int): Int? =
        raw?.let { value ->
            NUMBER_REGEX.findAll(value).map { it.value }.toList().getOrNull(index)?.toIntOrNull()
        }

    private fun extractInsideParens(text: String): String? =
        Regex("""\(([^)]+)\)""").find(text)?.groupValues?.getOrNull(1)

    // KBO WebForms id 는 `[id$=playerProfile_lblXxx]` 형태
    private fun Element.lbl(suffix: String): String? =
        selectFirst("[id\$=playerProfile_$suffix]")?.text()?.trim()?.takeIf { it.isNotEmpty() }

    private companion object {
        val THROW_REGEX = Regex("[좌우양]투")
        val BAT_REGEX = Regex("[좌우양]타")
        val FULL_YEAR_REGEX = Regex("(19|20)\\d{2}")
        val SHORT_YEAR_REGEX = Regex("^(\\d{2})")
        val NUMBER_REGEX = Regex("\\d+")
    }
}
