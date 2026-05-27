package com.kbo.summary.crawler.service

import com.kbo.summary.core.domain.HitterStat
import com.kbo.summary.core.domain.PitcherStat
import com.kbo.summary.core.domain.Player
import com.kbo.summary.core.domain.PlayerType
import com.kbo.summary.crawler.client.KboWebClient
import com.kbo.summary.crawler.parser.HitterBasicListParser
import com.kbo.summary.crawler.parser.PitcherBasicListParser
import com.kbo.summary.crawler.parser.PlayerProfileParser
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
    private val hitterBasicListParser: HitterBasicListParser,
    private val pitcherBasicListParser: PitcherBasicListParser,
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

    // 타자 시즌 기록 — Basic1+Basic2 두 페이지를 합쳐 페이지에 노출된 모든 선수의 기록을 upsert.
    // 시즌기록 페이지에 선수명·팀이 같이 있어 Player 마스터도 함께 upsert 한다 (로스터 크롤 전이라도 이름 표시 가능)
    suspend fun crawlHitterSeasonStats() {
        val basic1 = kboWebClient.get(HITTER_BASIC1_PATH)
        val basic2 = kboWebClient.get(HITTER_BASIC2_PATH)
        val rows = hitterBasicListParser.parse(basic1, basic2)
        val season = LocalDate.now().year
        rows.forEach { row ->
            upsertPlayerMaster(row.playerId, row.playerName, row.teamCode, PlayerType.HITTER)
            val entity = (hitterStatRepository.findByPlayerIdAndSeason(row.playerId, season)
                ?: HitterStat(playerId = row.playerId, season = season)).apply {
                avg = row.avg
                games = row.games
                ab = row.ab
                hits = row.hits
                doubles = row.doubles
                triples = row.triples
                hr = row.hr
                rbi = row.rbi
                runs = row.runs
                bb = row.bb
                so = row.so
                ops = row.ops
            }
            hitterStatRepository.save(entity)
        }
        log.info("타자 시즌기록 {}건 저장 ({}시즌)", rows.size, season)
    }

    // 투수 시즌 기록 — Basic1 한 페이지에서 모든 필드를 얻는다
    suspend fun crawlPitcherSeasonStats() {
        val html = kboWebClient.get(PITCHER_BASIC1_PATH)
        val rows = pitcherBasicListParser.parse(html)
        val season = LocalDate.now().year
        rows.forEach { row ->
            upsertPlayerMaster(row.playerId, row.playerName, row.teamCode, PlayerType.PITCHER)
            val entity = (pitcherStatRepository.findByPlayerIdAndSeason(row.playerId, season)
                ?: PitcherStat(playerId = row.playerId, season = season)).apply {
                era = row.era
                games = row.games
                wins = row.wins
                losses = row.losses
                saves = row.saves
                holds = row.holds
                ip = row.ip
                hits = row.hits
                so = row.so
                bb = row.bb
                whip = row.whip
            }
            pitcherStatRepository.save(entity)
        }
        log.info("투수 시즌기록 {}건 저장 ({}시즌)", rows.size, season)
    }

    // 시즌기록 페이지에서 얻은 정보로 Player 마스터를 upsert.
    // 시즌통계 페이지의 선수명은 매우 신뢰할 수 있어 기존 이름을 덮어쓴다 — 옛 PlayerProfileParser 가
    // title fallback 으로 "타자"/"투수" 같은 오염된 값을 저장해뒀더라도 여기서 교정된다.
    private fun upsertPlayerMaster(playerId: String, name: String, teamCode: String, type: PlayerType) {
        if (name.isEmpty()) return
        val existing = playerRepository.findByIdOrNull(playerId)
        val player = existing?.apply {
            playerName = name
            if (teamCode.isNotEmpty()) this.teamCode = teamCode
        } ?: Player(
            playerId = playerId,
            playerName = name,
            playerType = type,
            teamCode = teamCode.ifEmpty { null },
        )
        playerRepository.save(player)
    }

    private fun parseBirthDate(raw: String?): LocalDate? {
        val digits = raw?.filter { it.isDigit() } ?: return null
        if (digits.length != 8) return null
        return runCatching { LocalDate.parse(digits, DateTimeFormatter.BASIC_ISO_DATE) }.getOrNull()
    }

    private companion object {
        const val HITTER_PROFILE_PATH = "/Record/Player/HitterDetail/Basic.aspx"
        const val PITCHER_PROFILE_PATH = "/Record/Player/PitcherDetail/Basic.aspx"
        const val HITTER_BASIC1_PATH = "/Record/Player/HitterBasic/Basic1.aspx"
        const val HITTER_BASIC2_PATH = "/Record/Player/HitterBasic/Basic2.aspx"
        const val PITCHER_BASIC1_PATH = "/Record/Player/PitcherBasic/Basic1.aspx"
    }
}
