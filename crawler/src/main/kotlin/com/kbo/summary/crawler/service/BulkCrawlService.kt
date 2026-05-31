package com.kbo.summary.crawler.service

import com.kbo.summary.core.domain.GameStatus
import com.kbo.summary.crawler.parser.KBO_TEAM_CODES
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BulkCrawlService(
    private val gameCrawlerService: GameCrawlerService,
    private val playerCrawlerService: PlayerCrawlerService,
    private val teamCrawlerService: TeamCrawlerService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * [from, to] 날짜 범위의 경기 일정 + 종료된 경기 스코어를 순차 수집.
     * KBO 서버 부하 방지용 delay 를 각 요청 사이에 삽입한다.
     */
    fun crawlGamesInRange(from: LocalDate, to: LocalDate): BulkCrawlResult = runBlocking {
        log.info("[벌크크롤] 경기 일정·스코어 수집 시작: {} ~ {}", from, to)
        var gameDays = 0
        var gameCount = 0
        var scoreCount = 0
        var errorCount = 0

        var cursor = from
        while (!cursor.isAfter(to)) {
            runCatching {
                val games = gameCrawlerService.crawlGamesOn(cursor)
                gameDays++
                gameCount += games.size
                delay(CRAWL_DELAY_MS)

                games.filter { it.status == GameStatus.FINISHED }.forEach { game ->
                    runCatching {
                        gameCrawlerService.crawlGameScore(game.gameId)
                        scoreCount++
                        delay(CRAWL_DELAY_MS)
                    }.onFailure { e ->
                        log.warn("[벌크크롤] 스코어 수집 실패 ({}): {}", game.gameId, e.message)
                        errorCount++
                    }
                    runCatching {
                        gameCrawlerService.crawlAndSaveBoxScore(game.gameId, game.awayTeamCode, game.homeTeamCode)
                        delay(CRAWL_DELAY_MS)
                    }.onFailure { e ->
                        log.warn("[벌크크롤] 박스스코어 수집 실패 ({}): {}", game.gameId, e.message)
                        errorCount++
                    }
                    runCatching {
                        gameCrawlerService.crawlAndSaveHighlight(game.gameId)
                        delay(CRAWL_DELAY_MS)
                    }.onFailure { e ->
                        log.warn("[벌크크롤] 하이라이트 수집 실패 ({}): {}", game.gameId, e.message)
                        errorCount++
                    }
                }
            }.onFailure { e ->
                log.warn("[벌크크롤] 날짜 {} 경기 목록 실패: {}", cursor, e.message)
                errorCount++
            }

            cursor = cursor.plusDays(1)
        }

        BulkCrawlResult(gameDays, gameCount, scoreCount, errorCount).also {
            log.info("[벌크크롤] 완료: {}", it)
        }
    }

    /**
     * 전 팀 로스터 + 타자/투수 시즌 기록을 한 번에 수집.
     */
    fun crawlAllPlayers(): BulkCrawlResult = runBlocking {
        log.info("[벌크크롤] 선수 전체 수집 시작")
        var count = 0
        var errorCount = 0

        // 팀 로스터 (10개 팀)
        KBO_TEAM_CODES.values.distinct().forEach { teamCode ->
            runCatching {
                teamCrawlerService.crawlTeamRoster(teamCode)
                count++
                delay(CRAWL_DELAY_MS)
            }.onFailure { e ->
                log.warn("[벌크크롤] 로스터 실패 ({}): {}", teamCode, e.message)
                errorCount++
            }
        }

        // 타자/투수 시즌 기록 — 한 번 호출로 전 선수 upsert
        runCatching {
            playerCrawlerService.crawlHitterSeasonStats()
            delay(CRAWL_DELAY_MS)
            playerCrawlerService.crawlPitcherSeasonStats()
            delay(CRAWL_DELAY_MS)
            teamCrawlerService.crawlTeamSeasonStats()
            teamCrawlerService.crawlStandings()
            count += 4
        }.onFailure { e ->
            log.warn("[벌크크롤] 시즌기록 실패: {}", e.message)
            errorCount++
        }

        BulkCrawlResult(gameDays = 0, gameCount = 0, scoreCount = count, errorCount = errorCount).also {
            log.info("[벌크크롤] 선수 수집 완료: {}", it)
        }
    }

    private companion object {
        const val CRAWL_DELAY_MS = 300L
    }
}

data class BulkCrawlResult(
    val gameDays: Int,
    val gameCount: Int,
    val scoreCount: Int,
    val errorCount: Int,
)
