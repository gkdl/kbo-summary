package com.kbo.summary.crawler.service

import com.kbo.summary.core.domain.Player
import com.kbo.summary.core.domain.PlayerType
import com.kbo.summary.core.domain.Standing
import com.kbo.summary.crawler.client.KboWebClient
import com.kbo.summary.crawler.parser.TeamHitterParser
import com.kbo.summary.crawler.parser.TeamPitcherParser
import com.kbo.summary.crawler.parser.TeamRankParser
import com.kbo.summary.crawler.parser.TeamRosterParser
import com.kbo.summary.crawler.repository.PlayerRepository
import com.kbo.summary.crawler.repository.StandingRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TeamCrawlerService(
    private val kboWebClient: KboWebClient,
    private val teamRosterParser: TeamRosterParser,
    private val teamHitterParser: TeamHitterParser,
    private val teamPitcherParser: TeamPitcherParser,
    private val teamRankParser: TeamRankParser,
    private val playerRepository: PlayerRepository,
    private val standingRepository: StandingRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun crawlTeamRoster(teamCode: String) {
        val html = kboWebClient.get("$ROSTER_PATH?teamCode=$teamCode")
        val roster = teamRosterParser.parseRoster(html)
        roster.players.forEach { entry ->
            val type = if (entry.positionGroup == "투수") PlayerType.PITCHER else PlayerType.HITTER
            // 기존 선수는 로스터 필드만 갱신해 프로필 정보를 보존한다
            val player = playerRepository.findByIdOrNull(entry.playerId)?.apply {
                playerName = entry.name.ifEmpty { playerName }
                this.teamCode = teamCode
                backNumber = entry.backNumber ?: backNumber
                position = entry.positionGroup.ifEmpty { position }
                playerType = type
            } ?: Player(
                playerId = entry.playerId,
                playerName = entry.name,
                playerType = type,
                teamCode = teamCode,
                position = entry.positionGroup.ifEmpty { null },
                backNumber = entry.backNumber,
            )
            playerRepository.save(player)
        }
        log.info("팀 {} 로스터 {}명 저장", teamCode, roster.players.size)
    }

    // 팀 시즌 기록 — 영속화 엔티티가 없어 로그로만 남긴다
    suspend fun crawlTeamSeasonStats() {
        runCatching {
            val rows = teamHitterParser.parse(kboWebClient.get(TEAM_HITTER_PATH))
            log.info("팀 타격 통계 {}팀 수집", rows.size)
        }.onFailure { log.error("팀 타격 통계 수집 실패: {}", it.message) }
        runCatching {
            val rows = teamPitcherParser.parse(kboWebClient.get(TEAM_PITCHER_PATH))
            log.info("팀 투수 통계 {}팀 수집", rows.size)
        }.onFailure { log.error("팀 투수 통계 수집 실패: {}", it.message) }
    }

    // 팀 순위 — TeamRankDaily.aspx HTML 파싱 후 시즌별로 upsert (기존 행 제거 후 재삽입)
    suspend fun crawlStandings(): List<Standing> {
        val html = kboWebClient.get(STANDINGS_PATH)
        val rows = teamRankParser.parse(html)
        rows.map { it.season }.distinct().forEach { season ->
            standingRepository.findBySeasonOrderByRank(season)
                .takeIf { it.isNotEmpty() }
                ?.let { standingRepository.deleteAll(it) }
        }
        val saved = standingRepository.saveAll(
            rows.map { row ->
                Standing(
                    season = row.season,
                    teamCode = row.teamCode,
                    rank = row.rank,
                    wins = row.wins,
                    losses = row.losses,
                    draws = row.draws,
                    winRate = row.winRate,
                    gamesBehind = row.gamesBehind,
                )
            },
        )
        log.info("팀 순위 {}건 저장", saved.size)
        return saved
    }

    private companion object {
        const val ROSTER_PATH = "/Player/Register.aspx"
        const val TEAM_HITTER_PATH = "/Record/Team/Hitter/Basic1.aspx"
        const val TEAM_PITCHER_PATH = "/Record/Team/Pitcher/Basic1.aspx"
        const val STANDINGS_PATH = "/Record/TeamRank/TeamRankDaily.aspx"
    }
}
