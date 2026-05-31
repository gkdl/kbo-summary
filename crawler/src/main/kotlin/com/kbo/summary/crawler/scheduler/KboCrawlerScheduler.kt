package com.kbo.summary.crawler.scheduler

import com.kbo.summary.core.domain.GameStatus
import com.kbo.summary.crawler.parser.KBO_TEAM_CODES
import com.kbo.summary.crawler.repository.GameBoxHitterRepository
import com.kbo.summary.crawler.repository.GameHighlightRepository
import com.kbo.summary.crawler.service.GameCrawlerService
import com.kbo.summary.crawler.service.PlayerCrawlerService
import com.kbo.summary.crawler.service.TeamCrawlerService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalTime

@Configuration
@EnableScheduling
class CrawlerSchedulingConfig

@Component
class KboCrawlerScheduler(
    private val gameCrawlerService: GameCrawlerService,
    private val teamCrawlerService: TeamCrawlerService,
    private val playerCrawlerService: PlayerCrawlerService,
    private val gameHighlightRepository: GameHighlightRepository,
    private val gameBoxHitterRepository: GameBoxHitterRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // 오늘 경기 스코어 갱신 — 1분 주기, 14:00~23:00 사이에만 동작
    @Scheduled(fixedDelay = 60_000)
    fun refreshTodayScores() = runScheduled("오늘 경기 스코어 갱신") {
        if (LocalTime.now().hour !in ACTIVE_START_HOUR until ACTIVE_END_HOUR) {
            return@runScheduled
        }
        runBlocking {
            gameCrawlerService.crawlTodayGames().forEach { game ->
                // 시작 전 경기는 스코어보드 데이터가 없어 KBO 가 code=200 응답을 준다 — 호출 자체를 skip
                if (game.status == GameStatus.SCHEDULED) return@forEach
                runCatching { gameCrawlerService.crawlGameScore(game.gameId) }
                    .onFailure { e ->
                        // KBO 가 boxscore publish 전이면 .NET 의 "입력 문자열의 형식이 잘못되었습니다" 를 던짐.
                        // 다음 사이클이 따라잡으므로 ERROR 가 아닌 WARN 으로 낮춤.
                        val msg = e.message ?: ""
                        if (msg.contains("입력 문자열의 형식") || msg.contains("스코어보드 응답 실패")) {
                            log.warn("스코어 미공개 (gameId={}): KBO 가 아직 boxscore 데이터 미발행", game.gameId)
                        } else {
                            log.error("스코어 갱신 실패 (gameId={}): {}", game.gameId, msg)
                        }
                    }
            }
        }
    }

    // 종료 경기 박스스코어·하이라이트 저장 — 5분 주기, 17:00~다음날 02:00
    // 경기 종료 후 KBO가 데이터를 publish하는 데 시차가 있어 넉넉하게 새벽 2시까지 동작
    @Scheduled(fixedDelay = 300_000)
    fun saveFinishedGameDetails() = runScheduled("종료 경기 박스스코어·하이라이트 저장") {
        val hour = LocalTime.now().hour
        val isActive = hour >= BOXSCORE_START_HOUR || hour < BOXSCORE_END_HOUR
        if (!isActive) return@runScheduled
        runBlocking {
            gameCrawlerService.crawlTodayGames()
                .filter { it.status == GameStatus.FINISHED }
                .forEach { game ->
                    // 이미 저장된 경기는 skip
                    if (gameBoxHitterRepository.findByGameId(game.gameId).isNotEmpty()) return@forEach
                    runCatching {
                        gameCrawlerService.crawlAndSaveBoxScore(game.gameId, game.awayTeamCode, game.homeTeamCode)
                    }.onFailure { log.warn("박스스코어 저장 실패 ({}): {}", game.gameId, it.message) }

                    if (gameHighlightRepository.findByGameId(game.gameId) == null) {
                        runCatching {
                            gameCrawlerService.crawlAndSaveHighlight(game.gameId)
                        }.onFailure { log.warn("하이라이트 저장 실패 ({}): {}", game.gameId, it.message) }
                    }
                }
        }
    }

    // 팀 로스터 갱신 — 매주 월요일 03:00
    @Scheduled(cron = "0 57 3 * * *")
    fun refreshTeamRosters() = runScheduled("팀 로스터 갱신") {
        runBlocking {
            teamCodes().forEach { teamCode ->
                runCatching { teamCrawlerService.crawlTeamRoster(teamCode) }
                    .onFailure { log.error("로스터 갱신 실패 ({}): {}", teamCode, it.message) }
            }
        }
    }

    // 선수/팀 시즌 기록 갱신 — 매일 06:00. 한 페이지에서 22명씩 한 번에 수집되므로 선수별 호출 없음
    @Scheduled(cron = "0 57 3 * * *")
    fun refreshSeasonStats() = runScheduled("선수/팀 시즌 기록 갱신") {
        runBlocking {
            runCatching { playerCrawlerService.crawlHitterSeasonStats() }
                .onFailure { log.error("타자 시즌기록 갱신 실패: {}", it.message) }
            runCatching { playerCrawlerService.crawlPitcherSeasonStats() }
                .onFailure { log.error("투수 시즌기록 갱신 실패: {}", it.message) }
            runCatching { teamCrawlerService.crawlTeamSeasonStats() }
                .onFailure { log.error("팀 시즌기록 갱신 실패: {}", it.message) }
        }
    }

    // 팀 순위 갱신 — 매일 09:00
    @Scheduled(cron = "0 59 3 * * *")
    fun refreshStandings() = runScheduled("팀 순위 갱신") {
        runBlocking {
            runCatching { teamCrawlerService.crawlStandings() }
                .onFailure { log.error("팀 순위 갱신 실패: {}", it.message) }
        }
    }

    private fun teamCodes(): List<String> = KBO_TEAM_CODES.values.distinct()

    // 실패해도 다음 사이클이 계속되도록 예외를 잡아 에러 로그만 남긴다
    private fun runScheduled(label: String, block: () -> Unit) {
        try {
            log.info("[스케줄러] {} 시작", label)
            block()
            log.info("[스케줄러] {} 완료", label)
        } catch (e: Exception) {
            log.error("[스케줄러] {} 실패: {}", label, e.message, e)
        }
    }

    private companion object {
        const val ACTIVE_START_HOUR = 14
        const val ACTIVE_END_HOUR = 23
        const val BOXSCORE_START_HOUR = 17  // 박스스코어·하이라이트는 17:00~
        const val BOXSCORE_END_HOUR = 2     // ~02:00
    }
}
