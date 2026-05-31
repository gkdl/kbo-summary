package com.kbo.summary.api.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.kbo.summary.core.dto.GameDetailDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import java.time.Duration

/**
 * Google Gemini REST API 클라이언트 — KBO 경기 요약 생성용.
 *
 * 엔드포인트: POST /v1beta/models/{model}:generateContent?key={API_KEY}
 * - 인증은 query parameter `key` 로 전달 (헤더 아님)
 * - 무료 티어: gemini-2.5-flash-lite 기준 분당 15회, 일일 1,000회
 *   (2.0-flash 는 free quota=0, 2.5-flash 는 일시 503 빈도 높음, lite 가 가장 안정)
 * - 응답 추출 경로: candidates[0].content.parts[0].text
 */
@Component
class GeminiClient(
    @Value("\${gemini.api-key:}") private val apiKey: String,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val webClient: WebClient = WebClient.builder()
        .baseUrl(API_BASE_URL)
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create().responseTimeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS)),
            ),
        )
        .build()

    suspend fun generateSummary(gameData: GameDetailDto): String {
        if (apiKey.isBlank()) {
            log.warn("gemini.api-key 미설정 — 경기 요약을 생성할 수 없습니다.")
            return FALLBACK_MESSAGE
        }
        // 진단용 — 한 번의 사용자 클릭에 실제로 몇 번 Gemini 가 호출되는지 추적
        log.info("Gemini 호출 시작: gameId={}", gameData.game.gameId)
        // 분당 15회 한도는 60초가 지나야 풀린다. 짧은 재시도는 호출 수만 두 배로 늘리므로 단발 호출.
        return try {
            val responseJson = webClient.post()
                .uri { it.path("/v1beta/models/$MODEL:generateContent").queryParam("key", apiKey).build() }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildRequestBody(buildPrompt(gameData)))
                .retrieve()
                .awaitBody<String>()
            extractSummary(responseJson)
        } catch (e: WebClientResponseException) {
            if (e.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("Gemini 호출 한도 초과 — 분당/일일 free tier 한도 확인 필요")
            } else {
                log.error("Gemini API 호출 실패: {} {}", e.statusCode, e.responseBodyAsString.take(200))
            }
            FALLBACK_MESSAGE
        } catch (e: Exception) {
            log.error("Gemini API 호출 실패: {}", e.message)
            FALLBACK_MESSAGE
        }
    }

    private fun buildRequestBody(prompt: String): Map<String, Any> =
        mapOf(
            "contents" to listOf(
                mapOf("parts" to listOf(mapOf("text" to prompt))),
            ),
            "generationConfig" to mapOf(
                "maxOutputTokens" to MAX_OUTPUT_TOKENS,
                "temperature" to TEMPERATURE,
            ),
        )

    private fun buildPrompt(gameData: GameDetailDto): String {
        val game = gameData.game
        val awayName = TEAM_NAMES[game.awayTeamCode] ?: game.awayTeamCode
        val homeName = TEAM_NAMES[game.homeTeamCode] ?: game.homeTeamCode

        val innings = gameData.inningScores
            .joinToString(" | ") { "${it.inning}회: ${it.awayRuns}-${it.homeRuns}" }
            .ifEmpty { "정보 없음" }

        // 승리/패전/세이브 투수
        val allPitchers = gameData.awayPitchers + gameData.homePitchers
        val winPitcher = allPitchers.firstOrNull { it.decision == "승" }
            ?.let { "${teamName(it.teamCode)} ${it.playerName} (${it.inningsPitched}이닝 ${it.strikeOuts}K)" }
            ?: "정보 없음"
        val losePitcher = allPitchers.firstOrNull { it.decision == "패" }
            ?.let { "${teamName(it.teamCode)} ${it.playerName}" }
            ?: "정보 없음"
        val savePitcher = allPitchers.firstOrNull { it.decision == "세" }
            ?.let { "${teamName(it.teamCode)} ${it.playerName}" }
            ?: "없음"

        // 다득점 타자 상위 3명
        val topHitters = (gameData.awayHitters + gameData.homeHitters)
            .filter { it.hits > 0 || it.rbi > 0 }
            .sortedByDescending { it.rbi * 2 + it.hits }
            .take(3)
            .joinToString(", ") { "${teamName(it.teamCode)} ${it.playerName} ${it.atBats}타수${it.hits}안타 ${it.rbi}타점" }
            .ifEmpty { "정보 없음" }

        // 홈런 타자
        val homeRunHitters = (gameData.awayPitchers + gameData.homePitchers)
            .sumOf { it.homeRuns }
        // 박스스코어에 개인 홈런 수가 없으므로 피홈런 수로 언급 여부 판단
        val homeRunNote = if (homeRunHitters > 0) "이 경기에서 총 ${homeRunHitters}개의 홈런이 나왔습니다." else ""

        return """
            다음 KBO 경기 데이터를 바탕으로 3~4문장의 경기 요약을 작성해줘.
            스포츠 기자 말투로, 실제 데이터에 있는 선수명과 팀명만 사용해줘.
            데이터에 없는 내용은 절대 지어내지 마.

            [경기 결과]
            원정: $awayName ${game.awayScore ?: 0}점
            홈: $homeName ${game.homeScore ?: 0}점
            구장: ${game.stadium ?: ""}

            [이닝별 점수 (원정-홈)]
            $innings

            [투수]
            승리: $winPitcher
            패전: $losePitcher
            세이브: $savePitcher

            [주요 타자]
            $topHitters
            $homeRunNote
        """.trimIndent()
    }

    private fun teamName(code: String) = TEAM_NAMES[code] ?: code

    private fun extractSummary(responseJson: String): String {
        val text = objectMapper.readTree(responseJson)
            .path("candidates")
            .firstOrNull()
            ?.path("content")
            ?.path("parts")
            ?.firstOrNull()
            ?.path("text")
            ?.asText()
        return text?.trim()?.takeIf { it.isNotEmpty() } ?: FALLBACK_MESSAGE
    }

    companion object {
        const val FALLBACK_MESSAGE = "요약을 불러올 수 없습니다"

        private const val API_BASE_URL = "https://generativelanguage.googleapis.com"
        private const val MODEL = "gemini-2.5-flash-lite"
        private const val MAX_OUTPUT_TOKENS = 400
        private const val TEMPERATURE = 0.3  // 낮출수록 사실 기반, 높을수록 창의적
        private const val REQUEST_TIMEOUT_SECONDS = 30L

        val TEAM_NAMES = mapOf(
            "LG" to "LG 트윈스",
            "KT" to "KT 위즈",
            "SK" to "SSG 랜더스",
            "NC" to "NC 다이노스",
            "OB" to "두산 베어스",
            "HT" to "KIA 타이거즈",
            "LT" to "롯데 자이언츠",
            "SS" to "삼성 라이온즈",
            "HH" to "한화 이글스",
            "WO" to "키움 히어로즈",
        )
    }
}
