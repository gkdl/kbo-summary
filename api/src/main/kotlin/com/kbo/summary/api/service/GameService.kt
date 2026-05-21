package com.kbo.summary.api.service

import com.kbo.summary.core.domain.Game
import com.kbo.summary.core.domain.GameScore
import com.kbo.summary.core.domain.GameStatus
import com.kbo.summary.core.domain.GameSummary
import com.kbo.summary.core.dto.GameDetailDto
import com.kbo.summary.core.dto.GameDto
import com.kbo.summary.core.dto.GameSummaryDto
import com.kbo.summary.core.dto.InningScoreDto
import com.kbo.summary.core.dto.TeamLineDto
import com.kbo.summary.core.exception.GameNotFoundException
import com.kbo.summary.core.exception.SummaryException
import com.kbo.summary.crawler.repository.GameRepository
import com.kbo.summary.crawler.repository.GameScoreRepository
import com.kbo.summary.crawler.repository.GameSummaryRepository
import com.kbo.summary.crawler.service.GameCrawlerService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

private val crawlFallbackLog = LoggerFactory.getLogger("com.kbo.summary.api.service.CrawlFallback")

// 크롤러 서비스(suspend)를 동기 컨텍스트에서 호출한다. 크롤 실패는 로깅 후 무시하고 읽기를 이어간다.
internal fun crawlSafely(block: suspend () -> Unit) {
    try {
        runBlocking { block() }
    } catch (e: Exception) {
        crawlFallbackLog.warn("크롤 폴백 실패: {}", e.message)
    }
}

internal fun currentSeason(): Int = LocalDate.now().year

internal fun Game.toGameDto(): GameDto =
    GameDto(
        gameId = gameId,
        gameDate = gameDate,
        homeTeamCode = homeTeamCode,
        awayTeamCode = awayTeamCode,
        homeScore = homeScore,
        awayScore = awayScore,
        status = status.name,
        stadium = stadium,
        startTime = startTime,
    )

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val gameScoreRepository: GameScoreRepository,
    private val gameSummaryRepository: GameSummaryRepository,
    private val gameCrawlerService: GameCrawlerService,
) {
    fun getGamesByDate(date: LocalDate): List<GameDto> {
        var games = gameRepository.findByGameDate(date)
        if (games.isEmpty() && date == LocalDate.now()) {
            crawlSafely { gameCrawlerService.crawlTodayGames() }
            games = gameRepository.findByGameDate(date)
        }
        return games.map { it.toGameDto() }
    }

    fun getGameDetail(gameId: String): GameDetailDto {
        val game = gameRepository.findByGameId(gameId)
            ?: throw GameNotFoundException(gameId)
        var scores = gameScoreRepository.findByGameId(gameId)
        if (scores.isEmpty()) {
            crawlSafely { gameCrawlerService.crawlGameScore(gameId) }
            scores = gameScoreRepository.findByGameId(gameId)
        }
        return buildGameDetail(game, scores)
    }

    fun getGameSummary(gameId: String): GameSummaryDto {
        gameSummaryRepository.findByGameId(gameId)?.let { return it.toDto() }

        val game = gameRepository.findByGameId(gameId)
            ?: throw GameNotFoundException(gameId)
        if (game.status != GameStatus.FINISHED) {
            throw SummaryException("종료된 경기만 요약할 수 있습니다: $gameId")
        }
        val generated = GameSummary(gameId = gameId, summary = generateSummary(game))
        return gameSummaryRepository.save(generated).toDto()
    }

    private fun buildGameDetail(game: Game, scores: List<GameScore>): GameDetailDto {
        val ordered = scores.sortedBy { it.inning }
        val innings = ordered
            .filter { it.inning > 0 }
            .map { InningScoreDto(it.inning, it.homeScore, it.awayScore) }
        val totals = ordered.lastOrNull()
        return GameDetailDto(
            game = game.toGameDto(),
            inningScores = innings,
            homeLine = TeamLineDto(
                runs = totals?.homeR ?: 0,
                hits = totals?.homeH ?: 0,
                errors = totals?.homeE ?: 0,
                walks = totals?.homeB ?: 0,
            ),
            awayLine = TeamLineDto(
                runs = totals?.awayR ?: 0,
                hits = totals?.awayH ?: 0,
                errors = totals?.awayE ?: 0,
                walks = totals?.awayB ?: 0,
            ),
        )
    }

    // 실제 LLM 연동 전 임시 구현: 경기 데이터 기반 템플릿 요약을 생성한다.
    private fun generateSummary(game: Game): String {
        val homeScore = game.homeScore ?: 0
        val awayScore = game.awayScore ?: 0
        val result = when {
            homeScore > awayScore -> "${game.homeTeamCode}가 $homeScore-$awayScore 로 승리"
            awayScore > homeScore -> "${game.awayTeamCode}가 $awayScore-$homeScore 로 승리"
            else -> "$homeScore-$awayScore 무승부"
        }
        return "${game.gameDate} ${game.awayTeamCode}(원정) vs ${game.homeTeamCode}(홈) 경기는 $result 로 마무리됐습니다."
    }

    private fun GameSummary.toDto(): GameSummaryDto =
        GameSummaryDto(gameId = gameId, summary = summary, createdAt = createdAt)
}
