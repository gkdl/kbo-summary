package com.kbo.summary.crawler.service

import com.kbo.summary.core.domain.Player
import com.kbo.summary.core.domain.PlayerType
import com.kbo.summary.crawler.client.KboWebClient
import com.kbo.summary.crawler.parser.TeamRosterParser
import com.kbo.summary.crawler.parser.TeamStatParser
import com.kbo.summary.crawler.repository.PlayerRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TeamCrawlerService(
    private val kboWebClient: KboWebClient,
    private val teamStatParser: TeamStatParser,
    private val teamRosterParser: TeamRosterParser,
    private val playerRepository: PlayerRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // 팀 기록 전용 엔티티가 STEP 3 스키마에 없어 수집 결과는 로그로만 남긴다
    suspend fun crawlTeamStats(teamCode: String) {
        val hitterJson = kboWebClient.post(TEAM_HITTER_PATH, mapOf("teamCode" to teamCode))
        val hitter = teamStatParser.parseTeamHitterStat(hitterJson)
        val pitcherJson = kboWebClient.post(TEAM_PITCHER_PATH, mapOf("teamCode" to teamCode))
        val pitcher = teamStatParser.parseTeamPitcherStat(pitcherJson)
        log.info(
            "팀 {} 시즌기록 수집 — 타율 {}, 홈런 {}, ERA {}",
            teamCode, hitter.avg, hitter.homeRuns, pitcher.era,
        )
    }

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

    private companion object {
        // 실제 KBO 엔드포인트 경로 — 운영 전 검증 필요
        const val TEAM_HITTER_PATH = "/ws/Record.asmx/GetTeamHitterBasicRecord"
        const val TEAM_PITCHER_PATH = "/ws/Record.asmx/GetTeamPitcherBasicRecord"
        const val ROSTER_PATH = "/Player/Register.aspx"
    }
}
