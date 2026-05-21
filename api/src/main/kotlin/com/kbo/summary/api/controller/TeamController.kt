package com.kbo.summary.api.controller

import com.kbo.summary.api.service.TeamService
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.dto.HeadToHeadDto
import com.kbo.summary.core.dto.RecentFormDto
import com.kbo.summary.core.dto.TeamDetailDto
import com.kbo.summary.core.dto.TeamRosterDto
import com.kbo.summary.core.dto.TeamStatsDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/teams")
class TeamController(
    private val teamService: TeamService,
) {
    @GetMapping
    fun getTeams(): ApiResponse<List<TeamDetailDto>> =
        ApiResponse.ok(teamService.getAllTeams())

    @GetMapping("/{teamCode}")
    @Cacheable("teamDetail")
    fun getTeam(@PathVariable teamCode: String): ApiResponse<TeamDetailDto> =
        ApiResponse.ok(teamService.getTeamDetail(teamCode))

    @GetMapping("/{teamCode}/roster")
    fun getRoster(@PathVariable teamCode: String): ApiResponse<TeamRosterDto> =
        ApiResponse.ok(teamService.getTeamRoster(teamCode))

    @GetMapping("/{teamCode}/stats")
    fun getStats(@PathVariable teamCode: String): ApiResponse<TeamStatsDto> =
        ApiResponse.ok(teamService.getTeamStats(teamCode))

    @GetMapping("/{teamCode}/recent-form")
    fun getRecentForm(
        @PathVariable teamCode: String,
        @RequestParam(defaultValue = "10") n: Int,
    ): ApiResponse<RecentFormDto> =
        ApiResponse.ok(RecentFormDto(teamCode, teamService.getRecentForm(teamCode, n)))

    @GetMapping("/{teamCode}/head-to-head/{opponentCode}")
    fun getHeadToHead(
        @PathVariable teamCode: String,
        @PathVariable opponentCode: String,
    ): ApiResponse<HeadToHeadDto> =
        ApiResponse.ok(teamService.getHeadToHead(teamCode, opponentCode))
}
