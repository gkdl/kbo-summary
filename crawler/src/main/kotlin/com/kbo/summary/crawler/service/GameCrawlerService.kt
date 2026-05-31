package com.kbo.summary.crawler.service

import com.kbo.summary.core.domain.Game
import com.kbo.summary.core.domain.GameBoxHitter
import com.kbo.summary.core.domain.GameBoxPitcher
import com.kbo.summary.core.domain.GameHighlight
import com.kbo.summary.core.domain.GameScore
import com.kbo.summary.crawler.client.KboWebClient
import com.kbo.summary.crawler.parser.BoxScoreDto
import com.kbo.summary.crawler.parser.BoxScoreParser
import com.kbo.summary.crawler.parser.HighlightDto
import com.kbo.summary.crawler.parser.HighlightParser
import com.kbo.summary.crawler.parser.ScheduleParser
import com.kbo.summary.crawler.parser.ScoreParser
import com.kbo.summary.crawler.repository.GameBoxHitterRepository
import com.kbo.summary.crawler.repository.GameBoxPitcherRepository
import com.kbo.summary.crawler.repository.GameHighlightRepository
import com.kbo.summary.crawler.repository.GameRepository
import com.kbo.summary.crawler.repository.GameScoreRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class GameCrawlerService(
    private val kboWebClient: KboWebClient,
    private val scheduleParser: ScheduleParser,
    private val scoreParser: ScoreParser,
    private val boxScoreParser: BoxScoreParser,
    private val highlightParser: HighlightParser,
    private val gameRepository: GameRepository,
    private val gameScoreRepository: GameScoreRepository,
    private val gameHighlightRepository: GameHighlightRepository,
    private val gameBoxHitterRepository: GameBoxHitterRepository,
    private val gameBoxPitcherRepository: GameBoxPitcherRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun crawlTodayGames(): List<Game> = crawlGamesOn(LocalDate.now())

    // /ws/Main.asmx/GetKboGameList — leId=1군, srId=정규시즌+포스트시즌 시리즈, date=YYYYMMDD
    // 지정된 날짜의 경기 5개 정도를 받아 점수·상태를 매번 새로 덮어쓴다 (옛 잘못된 데이터 자동 교정).
    suspend fun crawlGamesOn(date: LocalDate): List<Game> {
        val dateStr = date.format(DateTimeFormatter.BASIC_ISO_DATE)
        val json = kboWebClient.post(
            SCHEDULE_PATH,
            mapOf(
                "leId" to "1",
                "srId" to "0,1,3,4,5,6,7,8,9",
                "date" to dateStr,
            ),
        )
        val games = scheduleParser.parseSchedule(json).map { dto ->
            Game(
                gameId = dto.gameId,
                gameDate = dto.gameDate,
                homeTeamCode = dto.homeTeamCode,
                awayTeamCode = dto.awayTeamCode,
                homeScore = dto.homeScore,
                awayScore = dto.awayScore,
                status = dto.status,
                stadium = dto.stadium,
                startTime = dto.startTime,
            )
        }
        val saved = gameRepository.saveAll(games)
        log.info("{} 경기 {}건 수집/저장", dateStr, saved.size)
        return saved
    }

    // GameScore 엔티티는 이닝당 1행이므로 이닝 행들을 모두 저장하고 마지막 이닝 행을 반환한다
    suspend fun crawlGameScore(gameId: String): GameScore {
        // seasonId 는 gameId 앞 4자리(YYYY), 정규시즌 srId=0
        val json = kboWebClient.post(
            SCOREBOARD_PATH,
            mapOf(
                "leId" to "1",
                "srId" to "0",
                "seasonId" to gameId.take(4),
                "gameId" to gameId,
            ),
        )
        val dto = scoreParser.parseScore(json)

        gameScoreRepository.findByGameId(gameId)
            .takeIf { it.isNotEmpty() }
            ?.let { gameScoreRepository.deleteAll(it) }

        val rows = dto.innings.map { inning ->
            GameScore(
                gameId = gameId,
                inning = inning.inning,
                homeScore = inning.homeRuns,
                awayScore = inning.awayRuns,
                homeR = dto.homeR, homeH = dto.homeH, homeE = dto.homeE, homeB = dto.homeB,
                awayR = dto.awayR, awayH = dto.awayH, awayE = dto.awayE, awayB = dto.awayB,
            )
        }.ifEmpty {
            listOf(
                GameScore(
                    gameId = gameId, inning = 0, homeScore = 0, awayScore = 0,
                    homeR = dto.homeR, homeH = dto.homeH, homeE = dto.homeE, homeB = dto.homeB,
                    awayR = dto.awayR, awayH = dto.awayH, awayE = dto.awayE, awayB = dto.awayB,
                ),
            )
        }
        val saved = gameScoreRepository.saveAll(rows)
        log.info("경기 {} 스코어 {}개 이닝 저장", gameId, saved.size)
        return saved.last()
    }

    // BoxScore 전용 엔티티가 STEP 3 스키마에 없어 파싱 결과 DTO만 반환한다.
    // KBO 응답이 어웨이/홈 순으로 정렬되므로 호출자가 두 팀 코드를 함께 전달한다.
    suspend fun crawlBoxScore(gameId: String, awayTeamCode: String, homeTeamCode: String): BoxScoreDto {
        val json = kboWebClient.post(
            BOXSCORE_PATH,
            mapOf(
                "leId" to "1",
                "srId" to "0",
                "seasonId" to gameId.take(4),
                "gameId" to gameId,
            ),
        )
        return boxScoreParser.parseBoxScore(gameId, json, awayTeamCode, homeTeamCode)
    }

    // 종료 직후엔 영상 업로드 전이라 null 일 수 있다 — 호출자가 nullable 처리한다.
    suspend fun crawlHighlight(gameId: String): HighlightDto? {
        val json = kboWebClient.post(
            HIGHLIGHT_PATH,
            mapOf(
                "leId" to "1",
                "srId" to "0",
                "seasonId" to gameId.take(4),
                "gameId" to gameId,
            ),
        )
        return highlightParser.parse(json)
    }

    // 박스스코어를 크롤 후 DB에 저장 (기존 데이터 교체).
    suspend fun crawlAndSaveBoxScore(gameId: String, awayTeamCode: String, homeTeamCode: String): BoxScoreDto {
        val dto = crawlBoxScore(gameId, awayTeamCode, homeTeamCode)
        gameBoxHitterRepository.deleteByGameId(gameId)
        gameBoxPitcherRepository.deleteByGameId(gameId)
        val hitters = (dto.awayHitters + dto.homeHitters).map { h ->
            GameBoxHitter(
                gameId = gameId, teamCode = h.teamCode, playerName = h.playerName,
                battingOrder = h.battingOrder, position = h.position,
                atBats = h.atBats, hits = h.hits, rbi = h.rbi, runs = h.runs, avg = h.avg,
            )
        }
        val pitchers = (dto.awayPitchers + dto.homePitchers).map { p ->
            GameBoxPitcher(
                gameId = gameId, teamCode = p.teamCode, playerName = p.playerName,
                role = p.role, decision = p.decision,
                wins = p.wins, losses = p.losses, saves = p.saves,
                inningsPitched = p.inningsPitched, battersFaced = p.battersFaced,
                pitchCount = p.pitchCount, atBats = p.atBats, hits = p.hits,
                homeRuns = p.homeRuns, walks = p.walks, strikeOuts = p.strikeOuts,
                runs = p.runs, earnedRuns = p.earnedRuns, era = p.era,
            )
        }
        gameBoxHitterRepository.saveAll(hitters)
        gameBoxPitcherRepository.saveAll(pitchers)
        log.info("박스스코어 저장: gameId={} 타자{}명 투수{}명", gameId, hitters.size, pitchers.size)
        return dto
    }

    // 하이라이트를 크롤 후 DB에 저장. 영상 미업로드 시 null 반환.
    suspend fun crawlAndSaveHighlight(gameId: String): HighlightDto? {
        val dto = crawlHighlight(gameId) ?: return null
        val entity = gameHighlightRepository.findByGameId(gameId)
            ?.apply { youtubeVideoId = dto.youtubeVideoId; title = dto.title }
            ?: GameHighlight(gameId = gameId, youtubeVideoId = dto.youtubeVideoId, title = dto.title)
        gameHighlightRepository.save(entity)
        log.info("하이라이트 저장: gameId={} videoId={}", gameId, dto.youtubeVideoId)
        return dto
    }

    private companion object {
        // crawlTodayGames 는 검증 완료. SCOREBOARD/BOXSCORE 는 ASMX 메서드 존재 확인됐으나
        // 필수 파라미터 셋이 미검증이라 운영 전 외부 JS 추가 조사 필요.
        const val SCHEDULE_PATH = "/ws/Main.asmx/GetKboGameList"
        const val SCOREBOARD_PATH = "/ws/Schedule.asmx/GetScoreBoardScroll"
        const val BOXSCORE_PATH = "/ws/Schedule.asmx/GetBoxScoreScroll"
        const val HIGHLIGHT_PATH = "/ws/Schedule.asmx/GetHighLight"
    }
}
