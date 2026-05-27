package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

data class HighlightDto(
    val gameId: String,
    val youtubeVideoId: String,
    val title: String?,
)

/**
 * KBO 하이라이트 응답 파서 — POST /ws/Schedule.asmx/GetHighLight
 *
 * 응답:
 * {
 *   "highlight": [{ "BD_TT": "NC vs KT", "G_ID": "20260523NCKT0",
 *                   "FILE_LK": "<iframe ... src=\"https://www.youtube.com/embed/{videoId}\" ...>", ... }],
 *   "code": "100"
 * }
 *
 * 종료 직후엔 영상 업로드 전이라 highlight 배열이 빈 채로 오기도 한다. 그 경우 null 반환.
 */
@Component
class HighlightParser(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun parse(json: String): HighlightDto? {
        val root = objectMapper.parseTree(json)
        if (root.path("code").asText() != SUCCESS_CODE) {
            log.warn("하이라이트 응답 실패: {}", root.path("msg").asText())
            return null
        }
        val item = root.path("highlight").firstOrNull() ?: return null
        val fileLink = item.path("FILE_LK").asText().takeIf { it.isNotEmpty() } ?: return null
        val videoId = YOUTUBE_EMBED_REGEX.find(fileLink)?.groupValues?.getOrNull(1)
            ?: YOUTUBE_WATCH_REGEX.find(fileLink)?.groupValues?.getOrNull(1)
            ?: return null
        return HighlightDto(
            gameId = item.path("G_ID").asText(),
            youtubeVideoId = videoId,
            title = item.path("BD_TT").asText().takeIf { it.isNotEmpty() },
        )
    }

    private companion object {
        const val SUCCESS_CODE = "100"
        // src="https://www.youtube.com/embed/{ID}" 패턴
        val YOUTUBE_EMBED_REGEX = Regex("""youtube\.com/embed/([A-Za-z0-9_-]{6,})""")
        // 보조: youtube.com/watch?v={ID} 형태도 일부 응답에 등장 가능
        val YOUTUBE_WATCH_REGEX = Regex("""youtube\.com/watch\?v=([A-Za-z0-9_-]{6,})""")
    }
}
