package com.kbo.summary.crawler.service

import com.kbo.summary.core.domain.Game
import com.kbo.summary.core.domain.GameScore
import com.kbo.summary.core.domain.Standing
import com.kbo.summary.crawler.client.KboWebClient
import com.kbo.summary.crawler.parser.BoxScoreDto
import com.kbo.summary.crawler.parser.BoxScoreParser
import com.kbo.summary.crawler.parser.ScheduleParser
import com.kbo.summary.crawler.parser.ScoreParser
import com.kbo.summary.crawler.parser.StandingsParser
import com.kbo.summary.crawler.repository.GameRepository
import com.kbo.summary.crawler.repository.GameScoreRepository
import com.kbo.summary.crawler.repository.StandingRepository
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
    private val standingsParser: StandingsParser,
    private val gameRepository: GameRepository,
    private val gameScoreRepository: GameScoreRepository,
    private val standingRepository: StandingRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun crawlTodayGames(): List<Game> {
        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        val json = kboWebClient.post(SCHEDULE_PATH, mapOf("date" to date))
        val games = scheduleParser.parseSchedule(json).map { dto ->
            Game(
                gameId = dto.gameId,
                gameDate = dto.gameDate,
                homeTeamCode = dto.homeTeamCode,
                awayTeamCode = dto.awayTeamCode,
                status = dto.status,
                stadium = dto.stadium,
                startTime = dto.startTime,
            )
        }
        val saved = gameRepository.saveAll(games)
        log.info("오늘 경기 {}건 수집/저장", saved.size)
        return saved
    }

    // GameScore 엔티티는 이닝당 1행이므로 이닝 행들을 모두 저장하고 마지막 이닝 행을 반환한다
    suspend fun crawlGameScore(gameId: String): GameScore {
        val json = kboWebClient.post(SCOREBOARD_PATH, mapOf("gameId" to gameId))
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

    // BoxScore 전용 엔티티가 STEP 3 스키마에 없어 파싱 결과 DTO만 반환한다
    suspend fun crawlBoxScore(gameId: String): BoxScoreDto {
        val json = kboWebClient.post(BOXSCORE_PATH, mapOf("gameId" to gameId))
        return boxScoreParser.parseBoxScore(json)
    }

    suspend fun crawlStandings(): List<Standing> {
        val json = kboWebClient.post(STANDINGS_PATH, emptyMap())
        val dtos = standingsParser.parseStandings(json)
        dtos.map { it.season }.distinct().forEach { season ->
            standingRepository.findBySeasonOrderByRank(season)
                .takeIf { it.isNotEmpty() }
                ?.let { standingRepository.deleteAll(it) }
        }
        val standings = dtos.map { dto ->
            Standing(
                season = dto.season,
                teamCode = dto.teamCode,
                rank = dto.rank,
                wins = dto.wins,
                losses = dto.losses,
                draws = dto.draws,
                winRate = dto.winRate,
                gamesBehind = dto.gamesBehind,
            )
        }
        val saved = standingRepository.saveAll(standings)
        log.info("팀 순위 {}건 저장", saved.size)
        return saved
    }

    private companion object {
        // 실제 KBO 엔드포인트 경로 — 운영 전 검증 필요
        const val SCHEDULE_PATH = "/ws/Schedule.asmx/GetScheduleList"
        const val SCOREBOARD_PATH = "/ws/Game.asmx/GetScoreBoardScroll"
        const val BOXSCORE_PATH = "/ws/Game.asmx/GetBoxScoreScroll"
        const val STANDINGS_PATH = "/ws/Standings.asmx/GetStandings"
    }
}
