package com.kbo.summary.api.service

import org.springframework.stereotype.Component

/**
 * 금칙어 필터 — 글/댓글 등록 시 제목·본문에 금칙어가 포함되면 차단한다.
 * 공백·특수문자를 제거한 정규화 문자열에서 부분일치를 검사해 우회를 일부 방지한다.
 *
 * 목록은 운영하며 보강한다. (심한 비속어/욕설 중심의 최소 시드)
 */
@Component
class ProfanityFilter {

    fun containsProfanity(vararg texts: String): Boolean {
        val normalized = texts.joinToString(" ").let(::normalize)
        return BANNED_WORDS.any { normalized.contains(it) }
    }

    // 공백·일부 특수문자 제거 + 소문자화 (ㅅ.ㅂ 같은 우회 일부 차단)
    private fun normalize(text: String): String =
        text.lowercase().replace(NON_WORD_REGEX, "")

    private companion object {
        val NON_WORD_REGEX = Regex("[\\s.,_*\\-~`'\"^]+")

        // 최소 시드 — 운영하며 확장. 정규화 문자열 기준(공백/특수문자 제거됨)으로 매칭된다.
        val BANNED_WORDS = setOf(
            "시발", "씨발", "시바", "씨바", "ㅅㅂ", "ㅆㅂ",
            "병신", "ㅂㅅ", "지랄", "ㅈㄹ",
            "개새끼", "새끼", "좆", "좇", "엿먹",
            "보지", "자지", "섹스", "야동",
            "fuck", "shit", "bitch", "asshole",
        )
    }
}
