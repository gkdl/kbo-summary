package com.kbo.summary.api.service

import com.kbo.summary.core.domain.HitterStat
import com.kbo.summary.core.domain.PitcherStat
import com.kbo.summary.core.domain.Player
import com.kbo.summary.core.domain.PlayerType
import com.kbo.summary.core.dto.HittingLine
import com.kbo.summary.core.dto.PitchingLine
import com.kbo.summary.core.dto.PlayerProfileDto
import com.kbo.summary.core.dto.PlayerRankingDto
import com.kbo.summary.core.dto.PlayerSearchResultDto
import com.kbo.summary.core.dto.PlayerStatDto
import com.kbo.summary.core.exception.PlayerNotFoundException
import com.kbo.summary.core.exception.SearchEmptyException
import com.kbo.summary.crawler.repository.HitterStatRepository
import com.kbo.summary.crawler.repository.PitcherStatRepository
import com.kbo.summary.crawler.repository.PlayerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PlayerService(
    private val playerRepository: PlayerRepository,
    private val hitterStatRepository: HitterStatRepository,
    private val pitcherStatRepository: PitcherStatRepository,
) {
    fun getPlayerProfile(playerId: String): PlayerProfileDto {
        val player = playerRepository.findByIdOrNull(playerId)
            ?: throw PlayerNotFoundException(playerId)
        return player.toProfileDto()
    }

    fun getPlayerStats(playerId: String): PlayerStatDto {
        val player = playerRepository.findByIdOrNull(playerId)
            ?: throw PlayerNotFoundException(playerId)
        val season = currentSeason()
        return if (player.playerType == PlayerType.PITCHER) {
            PlayerStatDto(
                playerId = playerId,
                season = season,
                playerType = player.playerType.name,
                hitting = null,
                pitching = pitcherStatRepository
                    .findByPlayerIdAndSeason(playerId, season)?.toPitchingLine(),
            )
        } else {
            PlayerStatDto(
                playerId = playerId,
                season = season,
                playerType = player.playerType.name,
                hitting = hitterStatRepository
                    .findByPlayerIdAndSeason(playerId, season)?.toHittingLine(),
                pitching = null,
            )
        }
    }

    fun searchPlayers(keyword: String): List<PlayerSearchResultDto> {
        val trimmed = keyword.trim()
        if (trimmed.length < MIN_SEARCH_LENGTH) {
            throw SearchEmptyException(keyword)
        }
        return playerRepository.findByPlayerNameContaining(trimmed).map { it.toSearchDto() }
    }

    fun getPlayerRankings(category: String, type: String): List<PlayerRankingDto> {
        val season = currentSeason()
        val playersById = playerRepository.findAll().associateBy { it.playerId }
        val cat = category.lowercase()

        return if (type.lowercase() == "pitcher") {
            val ascending = cat in setOf("era", "whip")
            pitcherStatRepository.findAll()
                .filter { it.season == season }
                .let { stats ->
                    if (ascending) stats.sortedBy { pitcherMetric(it, cat) }
                    else stats.sortedByDescending { pitcherMetric(it, cat) }
                }
                .take(RANKING_SIZE)
                .mapIndexed { index, stat ->
                    rankingDto(index + 1, stat.playerId, playersById, cat, pitcherValueText(stat, cat))
                }
        } else {
            hitterStatRepository.findAll()
                .filter { it.season == season }
                .sortedByDescending { hitterMetric(it, cat) }
                .take(RANKING_SIZE)
                .mapIndexed { index, stat ->
                    rankingDto(index + 1, stat.playerId, playersById, cat, hitterValueText(stat, cat))
                }
        }
    }

    private fun rankingDto(
        rank: Int,
        playerId: String,
        players: Map<String, Player>,
        category: String,
        value: String,
    ): PlayerRankingDto {
        val player = players[playerId]
        return PlayerRankingDto(
            rank = rank,
            playerId = playerId,
            playerName = player?.playerName ?: playerId,
            teamCode = player?.teamCode,
            category = category,
            value = value,
        )
    }

    private fun hitterMetric(stat: HitterStat, category: String): Double = when (category) {
        "ops" -> stat.ops?.toDouble() ?: 0.0
        "hr", "homerun" -> stat.hr.toDouble()
        "rbi" -> stat.rbi.toDouble()
        "sb" -> stat.sb.toDouble()
        "hits" -> stat.hits.toDouble()
        else -> stat.avg?.toDouble() ?: 0.0
    }

    private fun hitterValueText(stat: HitterStat, category: String): String = when (category) {
        "ops" -> stat.ops?.toPlainString() ?: "-"
        "hr", "homerun" -> stat.hr.toString()
        "rbi" -> stat.rbi.toString()
        "sb" -> stat.sb.toString()
        "hits" -> stat.hits.toString()
        else -> stat.avg?.toPlainString() ?: "-"
    }

    private fun pitcherMetric(stat: PitcherStat, category: String): Double = when (category) {
        "whip" -> stat.whip?.toDouble() ?: Double.MAX_VALUE
        "wins", "w" -> stat.wins.toDouble()
        "saves", "sv" -> stat.saves.toDouble()
        "holds" -> stat.holds.toDouble()
        "so", "strikeouts" -> stat.so.toDouble()
        else -> stat.era?.toDouble() ?: Double.MAX_VALUE
    }

    private fun pitcherValueText(stat: PitcherStat, category: String): String = when (category) {
        "whip" -> stat.whip?.toPlainString() ?: "-"
        "wins", "w" -> stat.wins.toString()
        "saves", "sv" -> stat.saves.toString()
        "holds" -> stat.holds.toString()
        "so", "strikeouts" -> stat.so.toString()
        else -> stat.era?.toPlainString() ?: "-"
    }

    private fun Player.toProfileDto(): PlayerProfileDto =
        PlayerProfileDto(
            playerId = playerId,
            name = playerName,
            teamCode = teamCode,
            playerType = playerType.name,
            position = position,
            backNumber = backNumber,
        )

    private fun Player.toSearchDto(): PlayerSearchResultDto =
        PlayerSearchResultDto(
            playerId = playerId,
            name = playerName,
            teamCode = teamCode,
            playerType = playerType.name,
            position = position,
        )

    private fun HitterStat.toHittingLine(): HittingLine =
        HittingLine(
            avg = avg,
            games = games,
            atBats = ab,
            hits = hits,
            homeRuns = hr,
            rbi = rbi,
            runs = runs,
            stolenBases = sb,
            walks = bb,
            strikeOuts = so,
            ops = ops,
        )

    private fun PitcherStat.toPitchingLine(): PitchingLine =
        PitchingLine(
            era = era,
            games = games,
            wins = wins,
            losses = losses,
            saves = saves,
            holds = holds,
            inningsPitched = ip,
            hits = hits,
            strikeOuts = so,
            walks = bb,
            whip = whip,
        )

    private companion object {
        const val MIN_SEARCH_LENGTH = 2
        const val RANKING_SIZE = 10
    }
}
