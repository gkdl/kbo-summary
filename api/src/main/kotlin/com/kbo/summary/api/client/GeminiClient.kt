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
    private val throttle: GeminiThrottle,
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

        val gameId = gameData.game.gameId

        // throttle 가드 — 한도/네거티브 캐시/글로벌 일시정지에 걸리면 Gemini 를 호출하지 않고 즉시 fallback
        if (!throttle.tryAcquire(gameId)) {
            log.info("Gemini 호출 차단 (throttle): gameId={}", gameId)
            return FALLBACK_MESSAGE
        }

        // 진단용 — 한 번의 사용자 클릭에 실제로 몇 번 Gemini 가 호출되는지 추적
        log.info("Gemini 호출 시작: gameId={}", gameId)
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
                throttle.markRateLimited(gameId)
            } else {
                log.error("Gemini API 호출 실패: {} {}", e.statusCode, e.responseBodyAsString.take(200))
                throttle.markFailure(gameId)
            }
            FALLBACK_MESSAGE
        } catch (e: Exception) {
            log.error("Gemini API 호출 실패: {}", e.message)
            throttle.markFailure(gameId)
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
        val awayFinal = game.awayScore ?: 0
        val homeFinal = game.homeScore ?: 0

        // 이닝당 득점·누적 점수·경기 흐름을 모두 사전 계산해 AI 에게 제공한다.
        // flash-lite 모델이 산수 실수로 "역전/동점/결승점" 같은 표현을 잘못 쓰는 환각을 방지.
        val perInning = gameData.inningScores
            .joinToString("\n") { "${it.inning}회: $awayName ${it.awayRuns}점, $homeName ${it.homeRuns}점" }
            .ifEmpty { "정보 없음" }

        val cumulative = buildCumulativeScores(gameData.inningScores, awayName, homeName)
        val gameFlow = buildGameFlow(gameData.inningScores, awayName, homeName)
        val resultLine = winnerLine(awayName, awayFinal, homeName, homeFinal)

        // 이닝 합계가 최종 점수와 어긋나면 요약 품질이 떨어질 가능성 — 진단 로그
        val inningAwaySum = gameData.inningScores.sumOf { it.awayRuns }
        val inningHomeSum = gameData.inningScores.sumOf { it.homeRuns }
        if (gameData.inningScores.isNotEmpty() &&
            (inningAwaySum != awayFinal || inningHomeSum != homeFinal)) {
            log.warn(
                "이닝 합계와 최종 점수 불일치: gameId={} 이닝합({}-{}) vs 최종({}-{})",
                game.gameId, inningAwaySum, inningHomeSum, awayFinal, homeFinal,
            )
        }

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

        // 홈런 — 박스스코어에 개인 홈런 수가 없으므로 피홈런 수로 언급 여부 판단
        val homeRunHitters = (gameData.awayPitchers + gameData.homePitchers).sumOf { it.homeRuns }
        val homeRunNote = if (homeRunHitters > 0) "이 경기에서 총 ${homeRunHitters}개의 홈런이 나왔습니다." else ""

        return """
            다음 KBO 경기 데이터로 3~4문장의 경기 요약을 작성해줘.
            스포츠 기자 말투, 실제 데이터에 있는 숫자·선수명·팀명만 사용.

            ⚠ 절대 규칙:
            - 점수는 반드시 [최종 점수]의 숫자만 사용. 다른 점수 절대 만들지 마.
            - "역전/동점/결승점" 같은 표현은 [경기 흐름]에 명시된 것만 사용. 직접 계산·추측 금지.
            - 이닝 정보가 부족하면 흐름 묘사 생략하고 결과·선수 중심으로 작성.
            - 데이터에 없는 내용은 절대 지어내지 마.

            [최종 점수]
            $resultLine
            구장: ${game.stadium ?: ""}

            [이닝당 득점]
            $perInning

            [이닝 종료 시점 누적 점수]
            $cumulative

            [경기 흐름 (사전 계산됨)]
            $gameFlow

            [투수]
            승리: $winPitcher
            패전: $losePitcher
            세이브: $savePitcher

            [주요 타자]
            $topHitters
            $homeRunNote
        """.trimIndent()
    }

    private fun winnerLine(awayName: String, awayFinal: Int, homeName: String, homeFinal: Int): String {
        val base = "$awayName ${awayFinal}점 - $homeName ${homeFinal}점"
        return when {
            awayFinal > homeFinal -> "$base ($awayName 승)"
            homeFinal > awayFinal -> "$base ($homeName 승)"
            else -> "$base (무승부)"
        }
    }

    private fun buildCumulativeScores(
        innings: List<com.kbo.summary.core.dto.InningScoreDto>,
        awayName: String,
        homeName: String,
    ): String {
        if (innings.isEmpty()) return "정보 없음"
        var away = 0
        var home = 0
        return innings.sortedBy { it.inning }.joinToString("\n") {
            away += it.awayRuns
            home += it.homeRuns
            "${it.inning}회 종료: $awayName $away - $homeName $home"
        }
    }

    /** 선취점·동점·역전 시점을 사전 계산. AI 가 산수 실수로 잘못 쓰지 않도록. */
    private fun buildGameFlow(
        innings: List<com.kbo.summary.core.dto.InningScoreDto>,
        awayName: String,
        homeName: String,
    ): String {
        if (innings.isEmpty()) return "정보 없음"
        val events = mutableListOf<String>()
        var away = 0
        var home = 0
        var prevLeader: String? = null  // "원정" / "홈" / "동점"
        var firstScore = true

        for (inning in innings.sortedBy { it.inning }) {
            away += inning.awayRuns
            home += inning.homeRuns
            if (away == 0 && home == 0) continue

            if (firstScore && (away > 0 || home > 0)) {
                val who = when {
                    away > home -> awayName
                    home > away -> homeName
                    else -> null
                }
                if (who != null) events += "${inning.inning}회: $who 선취점"
                firstScore = false
            }

            val leader = when {
                away > home -> "원정"
                home > away -> "홈"
                else -> "동점"
            }
            if (prevLeader != null && prevLeader != leader) {
                val desc = when (leader) {
                    "동점" -> "${inning.inning}회: 동점 ($away-$home)"
                    "원정" -> "${inning.inning}회: $awayName 역전 ($away-$home)"
                    else -> "${inning.inning}회: $homeName 역전 ($away-$home)"
                }
                events += desc
            }
            prevLeader = leader
        }

        return if (events.isEmpty()) "리드 변화 없음 (경기 내내 한 팀이 리드)" else events.joinToString("\n")
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
        private const val TEMPERATURE = 0.1  // 낮을수록 사실 기반·산수 정확. 환각 최소화 목적으로 0.1.
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
