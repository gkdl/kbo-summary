package com.kbo.summary.api.service

import com.kbo.summary.core.domain.Game
import com.kbo.summary.core.domain.GameStatus
import com.kbo.summary.core.domain.Standing
import com.kbo.summary.core.dto.HeadToHeadDto
import com.kbo.summary.core.dto.RosterPlayerDto
import com.kbo.summary.core.dto.TeamDetailDto
import com.kbo.summary.core.dto.TeamRosterDto
import com.kbo.summary.core.dto.TeamStatsDto
import com.kbo.summary.core.exception.TeamNotFoundException
import com.kbo.summary.crawler.repository.GameRepository
import com.kbo.summary.crawler.repository.PlayerRepository
import com.kbo.summary.crawler.repository.StandingRepository
import com.kbo.summary.crawler.repository.TeamRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val playerRepository: PlayerRepository,
    private val gameRepository: GameRepository,
    private val standingRepository: StandingRepository,
) {
    fun getAllTeams(): List<TeamDetailDto> {
        val standings = standingsOfSeason().associateBy { it.teamCode }
        return teamRepository.findAll().map { team ->
            val standing = standings[team.teamCode]
            TeamDetailDto(
                teamCode = team.teamCode,
                teamName = team.teamName,
                stadium = team.stadium,
                teamColor = team.teamColor,
                rank = standing?.rank,
                wins = standing?.wins ?: 0,
                losses = standing?.losses ?: 0,
                draws = standing?.draws ?: 0,
            )
        }
    }

    fun getTeamDetail(teamCode: String): TeamDetailDto {
        val team = teamRepository.findByIdOrNull(teamCode)
            ?: throw TeamNotFoundException(teamCode)
        val standing = standingsOfSeason().firstOrNull { it.teamCode == teamCode }
        return TeamDetailDto(
            teamCode = team.teamCode,
            teamName = team.teamName,
            stadium = team.stadium,
            teamColor = team.teamColor,
            rank = standing?.rank,
            wins = standing?.wins ?: 0,
            losses = standing?.losses ?: 0,
            draws = standing?.draws ?: 0,
        )
    }

    fun getTeamRoster(teamCode: String): TeamRosterDto {
        val players = playerRepository.findByTeamCode(teamCode)
        return TeamRosterDto(
            teamCode = teamCode,
            players = players.map {
                RosterPlayerDto(it.playerId, it.playerName, it.backNumber, it.position)
            },
        )
    }

    fun getTeamStats(teamCode: String): TeamStatsDto {
        val standing = standingsOfSeason().firstOrNull { it.teamCode == teamCode }
            ?: throw TeamNotFoundException(teamCode)
        return TeamStatsDto(
            teamCode = standing.teamCode,
            season = standing.season,
            rank = standing.rank,
            wins = standing.wins,
            losses = standing.losses,
            draws = standing.draws,
            winRate = standing.winRate,
            gamesBehind = standing.gamesBehind,
        )
    }

    fun getRecentForm(teamCode: String, n: Int = 10): List<String> =
        finishedGamesOf(teamCode)
            .take(n)
            .map { resultFor(teamCode, it) }

    fun getHeadToHead(teamA: String, teamB: String): HeadToHeadDto {
        val season = currentSeason()
        val games = gameRepository.findAll()
            .filter { it.status == GameStatus.FINISHED && it.gameDate.year == season }
            .filter {
                (it.homeTeamCode == teamA && it.awayTeamCode == teamB) ||
                    (it.homeTeamCode == teamB && it.awayTeamCode == teamA)
            }
            .sortedByDescending { it.gameDate }

        var teamAWins = 0
        var teamBWins = 0
        var draws = 0
        for (game in games) {
            val aScore = (if (game.homeTeamCode == teamA) game.homeScore else game.awayScore) ?: 0
            val bScore = (if (game.homeTeamCode == teamB) game.homeScore else game.awayScore) ?: 0
            when {
                aScore > bScore -> teamAWins++
                bScore > aScore -> teamBWins++
                else -> draws++
            }
        }
        return HeadToHeadDto(teamA, teamB, teamAWins, teamBWins, draws, games.map { it.toGameDto() })
    }

    private fun standingsOfSeason(): List<Standing> =
        standingRepository.findBySeasonOrderByRank(currentSeason())

    private fun finishedGamesOf(teamCode: String): List<Game> =
        gameRepository.findAll()
            .filter {
                it.status == GameStatus.FINISHED &&
                    (it.homeTeamCode == teamCode || it.awayTeamCode == teamCode)
            }
            .sortedByDescending { it.gameDate }

    private fun resultFor(teamCode: String, game: Game): String {
        val isHome = game.homeTeamCode == teamCode
        val mine = (if (isHome) game.homeScore else game.awayScore) ?: 0
        val opponent = (if (isHome) game.awayScore else game.homeScore) ?: 0
        return when {
            mine > opponent -> "W"
            mine < opponent -> "L"
            else -> "D"
        }
    }
}
