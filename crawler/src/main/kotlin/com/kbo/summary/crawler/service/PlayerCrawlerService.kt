package com.kbo.summary.crawler.service

import com.kbo.summary.core.domain.HitterStat
import com.kbo.summary.core.domain.PitcherStat
import com.kbo.summary.core.domain.Player
import com.kbo.summary.core.domain.PlayerType
import com.kbo.summary.crawler.client.KboWebClient
import com.kbo.summary.crawler.parser.PlayerProfileParser
import com.kbo.summary.crawler.parser.PlayerRankingParser
import com.kbo.summary.crawler.parser.PlayerStatParser
import com.kbo.summary.crawler.repository.HitterStatRepository
import com.kbo.summary.crawler.repository.PitcherStatRepository
import com.kbo.summary.crawler.repository.PlayerRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class PlayerCrawlerService(
    private val kboWebClient: KboWebClient,
    private val playerProfileParser: PlayerProfileParser,
    private val playerStatParser: PlayerStatParser,
    private val playerRankingParser: PlayerRankingParser,
    private val playerRepository: PlayerRepository,
    private val hitterStatRepository: HitterStatRepository,
    private val pitcherStatRepository: PitcherStatRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun crawlPlayerProfile(playerId: String): Player {
        val existing = playerRepository.findByIdOrNull(playerId)
        val type = existing?.playerType ?: PlayerType.HITTER
        val path = if (type == PlayerType.PITCHER) PITCHER_PROFILE_PATH else HITTER_PROFILE_PATH
        val html = kboWebClient.get("$path?playerId=$playerId")
        val profile = if (type == PlayerType.PITCHER) {
            playerProfileParser.parsePitcherProfile(html)
        } else {
            playerProfileParser.parseHitterProfile(html)
        }

        // 파싱이 누락한 필드는 기존 값을 유지한다
        val player = (existing ?: Player(playerId, profile.name, type)).apply {
            if (profile.name.isNotEmpty()) playerName = profile.name
            profile.teamCode?.let { teamCode = it }
            profile.position?.let { position = it }
            profile.backNumber?.let { backNumber = it }
            profile.bats?.let { bats = it }
            profile.throws?.let { throws = it }
            parseBirthDate(profile.birthDate)?.let { birthDate = it }
            profile.height?.let { height = it }
            profile.weight?.let { weight = it }
            profile.school?.let { school = it }
            profile.debutYear?.let { debutYear = it }
        }
        val saved = playerRepository.save(player)
        log.info("선수 프로필 저장: {} ({})", saved.playerName, playerId)
        return saved
    }

    suspend fun crawlPlayerStat(playerId: String) {
        val type = playerRepository.findByIdOrNull(playerId)?.playerType ?: PlayerType.HITTER
        if (type == PlayerType.PITCHER) crawlPitcherStat(playerId) else crawlHitterStat(playerId)
    }

    private suspend fun crawlHitterStat(playerId: String) {
        val json = kboWebClient.post(HITTER_STAT_PATH, mapOf("playerId" to playerId))
        val dto = playerStatParser.parseHitterStat(json)
        val entity = (hitterStatRepository.findByPlayerIdAndSeason(playerId, dto.season)
            ?: HitterStat(playerId = playerId, season = dto.season)).apply {
            avg = dto.avg
            games = dto.games
            ab = dto.atBats
            hits = dto.hits
            doubles = dto.doubles
            triples = dto.triples
            hr = dto.homeRuns
            rbi = dto.rbi
            runs = dto.runs
            sb = dto.stolenBases
            bb = dto.walks
            so = dto.strikeOuts
            ops = dto.ops
        }
        hitterStatRepository.save(entity)
        log.info("타자 시즌기록 저장: {} ({}시즌)", playerId, dto.season)
    }

    private suspend fun crawlPitcherStat(playerId: String) {
        val json = kboWebClient.post(PITCHER_STAT_PATH, mapOf("playerId" to playerId))
        val dto = playerStatParser.parsePitcherStat(json)
        val entity = (pitcherStatRepository.findByPlayerIdAndSeason(playerId, dto.season)
            ?: PitcherStat(playerId = playerId, season = dto.season)).apply {
            era = dto.era
            games = dto.games
            wins = dto.wins
            losses = dto.losses
            saves = dto.saves
            holds = dto.holds
            ip = dto.inningsPitched?.toBigDecimalOrNull()
            hits = dto.hits
            so = dto.strikeOuts
            bb = dto.walks
            whip = dto.whip
        }
        pitcherStatRepository.save(entity)
        log.info("투수 시즌기록 저장: {} ({}시즌)", playerId, dto.season)
    }

    // 순위 전용 엔티티가 STEP 3 스키마에 없어 수집 결과는 영속화하지 않는다
    suspend fun crawlPlayerRankings() {
        HITTER_RANKING_CATEGORIES.forEach { category ->
            runCatching {
                val json = kboWebClient.post(HITTER_RANKING_PATH, mapOf("category" to category))
                playerRankingParser.parseHitterRanking(json)
            }.onSuccess { log.info("타자 {} 순위 {}건 수집", category, it.size) }
                .onFailure { log.error("타자 {} 순위 수집 실패: {}", category, it.message) }
        }
        PITCHER_RANKING_CATEGORIES.forEach { category ->
            runCatching {
                val json = kboWebClient.post(PITCHER_RANKING_PATH, mapOf("category" to category))
                playerRankingParser.parsePitcherRanking(json)
            }.onSuccess { log.info("투수 {} 순위 {}건 수집", category, it.size) }
                .onFailure { log.error("투수 {} 순위 수집 실패: {}", category, it.message) }
        }
    }

    private fun parseBirthDate(raw: String?): LocalDate? {
        val digits = raw?.filter { it.isDigit() } ?: return null
        if (digits.length != 8) return null
        return runCatching { LocalDate.parse(digits, DateTimeFormatter.BASIC_ISO_DATE) }.getOrNull()
    }

    private companion object {
        // 실제 KBO 엔드포인트 경로 — 운영 전 검증 필요
        const val HITTER_PROFILE_PATH = "/Record/Player/HitterDetail/Basic.aspx"
        const val PITCHER_PROFILE_PATH = "/Record/Player/PitcherDetail/Basic.aspx"
        const val HITTER_STAT_PATH = "/ws/Record.asmx/GetHitterBasicRecord"
        const val PITCHER_STAT_PATH = "/ws/Record.asmx/GetPitcherBasicRecord"
        const val HITTER_RANKING_PATH = "/ws/Record.asmx/GetHitterRanking"
        const val PITCHER_RANKING_PATH = "/ws/Record.asmx/GetPitcherRanking"

        // 타자 4종 + 투수 4종 순위 카테고리
        val HITTER_RANKING_CATEGORIES = listOf("AVG", "HR", "RBI", "SB")
        val PITCHER_RANKING_CATEGORIES = listOf("ERA", "WIN", "SO", "SV")
    }
}
