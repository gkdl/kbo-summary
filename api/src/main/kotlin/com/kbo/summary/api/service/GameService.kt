package com.kbo.summary.api.service

import com.kbo.summary.api.client.GeminiClient
import com.kbo.summary.core.domain.Game
import com.kbo.summary.core.domain.GameScore
import com.kbo.summary.core.domain.GameStatus
import com.kbo.summary.core.domain.GameSummary
import com.kbo.summary.core.dto.BoxHitterDto
import com.kbo.summary.core.dto.BoxPitcherDto
import com.kbo.summary.core.dto.GameDetailDto
import com.kbo.summary.core.dto.GameDto
import com.kbo.summary.core.dto.GameHighlightDto
import com.kbo.summary.core.dto.GameSummaryDto
import com.kbo.summary.core.dto.HighlightDto
import com.kbo.summary.core.dto.InningScoreDto
import com.kbo.summary.core.dto.TeamLineDto
import com.kbo.summary.crawler.parser.BoxScoreDto
import com.kbo.summary.crawler.parser.HitterRecordDto
import com.kbo.summary.crawler.parser.PitcherRecordDto
import com.kbo.summary.crawler.parser.HighlightDto as CrawlerHighlightDto
import com.kbo.summary.core.exception.GameNotFoundException
import com.kbo.summary.core.exception.SummaryException
import com.kbo.summary.crawler.repository.GameBoxHitterRepository
import com.kbo.summary.crawler.repository.GameBoxPitcherRepository
import com.kbo.summary.crawler.repository.GameHighlightRepository
import com.kbo.summary.crawler.repository.GameRepository
import com.kbo.summary.crawler.repository.GameScoreRepository
import com.kbo.summary.crawler.repository.GameSummaryRepository
import com.kbo.summary.crawler.service.GameCrawlerService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

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
    private val gameHighlightRepository: GameHighlightRepository,
    private val gameBoxHitterRepository: GameBoxHitterRepository,
    private val gameBoxPitcherRepository: GameBoxPitcherRepository,
    private val gameCrawlerService: GameCrawlerService,
    private val geminiClient: GeminiClient,
    private val gameCrawlThrottle: GameCrawlThrottle,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getGamesByDate(date: LocalDate): List<GameDto> {
        // 읽기 경로는 DB 만 조회한다. 오늘 경기의 라이브 갱신은 스케줄러가 담당.
        // DB 에 아직 없는 날짜(과거 첫 조회·미래 일정·스케줄러 비활성 시간)만 그 자리에서 1회 보충하고,
        // throttle 로 같은 날짜 연타 크롤(경기 없는 날 등)을 막는다.
        var games = gameRepository.findByGameDate(date)
        if (games.isEmpty() && gameCrawlThrottle.tryAcquire("games:$date")) {
            crawlSafely { gameCrawlerService.crawlGamesOn(date) }
            games = gameRepository.findByGameDate(date)
        }
        return games.map { it.toGameDto() }
    }

    fun getGameDetail(gameId: String): GameDetailDto {
        val game = gameRepository.findByGameId(gameId)
            ?: throw GameNotFoundException(gameId)
        var scores = gameScoreRepository.findByGameId(gameId)
        // 시작 전 경기는 GameScore 자체가 없음.
        // 진행 중(IN_PROGRESS) 경기는 이닝 점수가 계속 바뀌므로 조회할 때마다 갱신한다.
        //   - 스케줄러(14~23시)가 못 도는 시간대·서버 재시작·로컬 개발 환경에서도 라이브 점수가 최신이 되고,
        //   - 한 번 저장된 빈/부분 이닝 행이 영영 고정되는 문제(scores.isEmpty()=false 라 재크롤 안 되던 버그)를 해소한다.
        // 종료(FINISHED) 경기는 값이 안 바뀌므로 DB 가 비어 있을 때만 1회 보충한다.
        // throttle(60s)로 같은 경기 연타 크롤을 막아 KBO 과다호출은 방지한다.
        val refreshScore = game.status == GameStatus.IN_PROGRESS || scores.isEmpty()
        if (game.status != GameStatus.SCHEDULED && refreshScore &&
            gameCrawlThrottle.tryAcquire("score:$gameId")) {
            crawlSafely { gameCrawlerService.crawlGameScore(gameId) }
            scores = gameScoreRepository.findByGameId(gameId)
        }
        // 박스스코어: DB 우선, 없으면 크롤 후 저장. 시작 전 경기는 데이터 없음.
        val boxScore = if (game.status == GameStatus.SCHEDULED) null else fetchBoxScore(game)
        // 하이라이트: DB 우선, 없으면 크롤 후 저장. 종료 경기만.
        val highlight = if (game.status == GameStatus.FINISHED) fetchHighlight(game.gameId) else null
        return buildGameDetail(game, scores, boxScore, highlight)
    }

    private fun fetchBoxScore(game: Game): BoxScoreDto? {
        val cached = boxScoreFromDb(game)
        // 진행 중(IN_PROGRESS) 경기는 박스스코어도 매 이닝 바뀌므로 조회 때마다 갱신한다.
        // 그 외엔 DB 가 있으면 그대로 쓰고, 없을 때만 보충 크롤. throttle(60s)로 연타 크롤을 막는다.
        val needsRefresh = game.status == GameStatus.IN_PROGRESS || cached == null
        if (needsRefresh && gameCrawlThrottle.tryAcquire("box:${game.gameId}")) {
            runCatching {
                runBlocking { gameCrawlerService.crawlAndSaveBoxScore(game.gameId, game.awayTeamCode, game.homeTeamCode) }
            }.getOrNull()?.let { return it }
        }
        // 크롤을 건너뛰었거나(throttle) 실패하면 DB 캐시로 폴백한다.
        return cached
    }

    private fun boxScoreFromDb(game: Game): BoxScoreDto? {
        val dbHitters = gameBoxHitterRepository.findByGameId(game.gameId)
        if (dbHitters.isEmpty()) return null
        val dbPitchers = gameBoxPitcherRepository.findByGameId(game.gameId)
        return BoxScoreDto(
            gameId = game.gameId,
            awayHitters = dbHitters.filter { it.teamCode == game.awayTeamCode }.map { it.toRecordDto() },
            homeHitters = dbHitters.filter { it.teamCode == game.homeTeamCode }.map { it.toRecordDto() },
            awayPitchers = dbPitchers.filter { it.teamCode == game.awayTeamCode }.map { it.toRecordDto() },
            homePitchers = dbPitchers.filter { it.teamCode == game.homeTeamCode }.map { it.toRecordDto() },
        )
    }

    private fun fetchHighlight(gameId: String): CrawlerHighlightDto? {
        gameHighlightRepository.findByGameId(gameId)?.let {
            return CrawlerHighlightDto(gameId = it.gameId, youtubeVideoId = it.youtubeVideoId, title = it.title)
        }
        if (!gameCrawlThrottle.tryAcquire("highlight:$gameId")) return null
        return runCatching { runBlocking { gameCrawlerService.crawlAndSaveHighlight(gameId) } }.getOrNull()
    }

    /**
     * 해당 날짜의 종료된 경기들의 하이라이트만 모아 반환.
     * 하이라이트 영상이 없는 경기는 결과에서 제외.
     */
    fun getHighlightsByDate(date: LocalDate): List<GameHighlightDto> {
        val games = getGamesByDate(date)
        return games
            .filter { it.status == "FINISHED" }
            .mapNotNull { game ->
                val highlight = fetchHighlight(game.gameId) ?: return@mapNotNull null
                GameHighlightDto(
                    gameId = game.gameId,
                    gameDate = game.gameDate.toString(),
                    awayTeamCode = game.awayTeamCode,
                    homeTeamCode = game.homeTeamCode,
                    awayScore = game.awayScore,
                    homeScore = game.homeScore,
                    highlight = HighlightDto(
                        youtubeVideoId = highlight.youtubeVideoId,
                        title = highlight.title,
                    ),
                )
            }
    }

    fun getGameSummary(gameId: String): GameSummaryDto {
        gameSummaryRepository.findByGameId(gameId)?.let {
            log.info("요약 DB 캐시 hit (Gemini 호출 없이 재사용): gameId={}", gameId)
            return it.toDto()
        }

        val game = gameRepository.findByGameId(gameId)
            ?: throw GameNotFoundException(gameId)
        if (game.status != GameStatus.FINISHED) {
            throw SummaryException("종료된 경기만 요약할 수 있습니다: $gameId")
        }

        log.info("요약 신규 생성 (Gemini 호출): gameId={}", gameId)
        val detail = buildGameDetail(game, gameScoreRepository.findByGameId(gameId), fetchBoxScore(game), null)
        val summaryText = runBlocking { geminiClient.generateSummary(detail) }

        // 호출 실패 시 fallback 문구는 저장하지 않아 다음 요청에서 재시도된다
        if (summaryText == GeminiClient.FALLBACK_MESSAGE) {
            log.warn("요약 실패 — 다음 요청에서 재시도: gameId={}", gameId)
            return GameSummaryDto(gameId = gameId, summary = summaryText, createdAt = LocalDateTime.now())
        }
        val saved = gameSummaryRepository.save(GameSummary(gameId = gameId, summary = summaryText))
        log.info("요약 DB 저장 완료 (이후 모든 사용자가 재사용): gameId={}", gameId)
        return saved.toDto()
    }

    private fun buildGameDetail(
        game: Game,
        scores: List<GameScore>,
        boxScore: BoxScoreDto?,
        highlight: CrawlerHighlightDto?,
    ): GameDetailDto {
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
            awayHitters = boxScore?.awayHitters?.map { it.toDto() } ?: emptyList(),
            homeHitters = boxScore?.homeHitters?.map { it.toDto() } ?: emptyList(),
            awayPitchers = boxScore?.awayPitchers?.map { it.toDto() } ?: emptyList(),
            homePitchers = boxScore?.homePitchers?.map { it.toDto() } ?: emptyList(),
            highlight = highlight?.let { HighlightDto(youtubeVideoId = it.youtubeVideoId, title = it.title) },
        )
    }

    private fun HitterRecordDto.toDto(): BoxHitterDto = BoxHitterDto(
        playerName = playerName,
        battingOrder = battingOrder,
        position = position,
        teamCode = teamCode,
        atBats = atBats,
        hits = hits,
        rbi = rbi,
        runs = runs,
        avg = avg,
    )

    private fun PitcherRecordDto.toDto(): BoxPitcherDto = BoxPitcherDto(
        playerName = playerName,
        teamCode = teamCode,
        role = role,
        decision = decision,
        inningsPitched = inningsPitched,
        pitchCount = pitchCount,
        battersFaced = battersFaced,
        atBats = atBats,
        hits = hits,
        homeRuns = homeRuns,
        walks = walks,
        strikeOuts = strikeOuts,
        runs = runs,
        earnedRuns = earnedRuns,
        era = era,
    )

    private fun GameSummary.toDto(): GameSummaryDto =
        GameSummaryDto(gameId = gameId, summary = summary, createdAt = createdAt)
}

private fun com.kbo.summary.core.domain.GameBoxHitter.toRecordDto() = HitterRecordDto(
    playerName = playerName, battingOrder = battingOrder, position = position,
    teamCode = teamCode, atBats = atBats, hits = hits, rbi = rbi, runs = runs, avg = avg,
)

private fun com.kbo.summary.core.domain.GameBoxPitcher.toRecordDto() = PitcherRecordDto(
    playerName = playerName, teamCode = teamCode, role = role, decision = decision,
    wins = wins, losses = losses, saves = saves, inningsPitched = inningsPitched,
    battersFaced = battersFaced, pitchCount = pitchCount, atBats = atBats,
    hits = hits, homeRuns = homeRuns, walks = walks, strikeOuts = strikeOuts,
    runs = runs, earnedRuns = earnedRuns, era = era,
)
