package com.kbo.summary.crawler.scheduler

import com.kbo.summary.crawler.parser.KBO_TEAM_CODES
import com.kbo.summary.crawler.repository.PlayerRepository
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
    private val playerRepository: PlayerRepository,
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
                runCatching { gameCrawlerService.crawlGameScore(game.gameId) }
                    .onFailure { log.error("스코어 갱신 실패 (gameId={}): {}", game.gameId, it.message) }
            }
        }
    }

    // 팀/선수 시즌 기록 갱신 — 매일 06:00
    @Scheduled(cron = "0 0 6 * * *")
    fun refreshSeasonStats() = runScheduled("팀/선수 시즌 기록 갱신") {
        runBlocking {
            teamCodes().forEach { teamCode ->
                runCatching { teamCrawlerService.crawlTeamStats(teamCode) }
                    .onFailure { log.error("팀 기록 갱신 실패 ({}): {}", teamCode, it.message) }
            }
            playerRepository.findAll().forEach { player ->
                runCatching { playerCrawlerService.crawlPlayerStat(player.playerId) }
                    .onFailure { log.error("선수 기록 갱신 실패 ({}): {}", player.playerId, it.message) }
            }
        }
    }

    // 팀 순위 갱신 — 매일 09:00
    @Scheduled(cron = "0 0 9 * * *")
    fun refreshStandings() = runScheduled("팀 순위 갱신") {
        runBlocking { gameCrawlerService.crawlStandings() }
    }

    // 상대전적 캐시 갱신 — 매일 00:10
    @Scheduled(cron = "0 10 0 * * *")
    fun refreshHeadToHeadCache() = runScheduled("상대전적 캐시 갱신") {
        // 상대전적 캐시 서비스는 아직 구현되지 않음 — 전용 서비스 추가 시 호출 연결 예정
        log.info("상대전적 캐시 갱신 — 구현 대기 중")
    }

    // 팀 로스터 갱신 — 매주 월요일 03:00
    @Scheduled(cron = "0 0 3 * * MON")
    fun refreshTeamRosters() = runScheduled("팀 로스터 갱신") {
        runBlocking {
            teamCodes().forEach { teamCode ->
                runCatching { teamCrawlerService.crawlTeamRoster(teamCode) }
                    .onFailure { log.error("로스터 갱신 실패 ({}): {}", teamCode, it.message) }
            }
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
    }
}
