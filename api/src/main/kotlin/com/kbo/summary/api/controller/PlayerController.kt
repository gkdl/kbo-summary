package com.kbo.summary.api.controller

import com.kbo.summary.api.service.PlayerService
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.dto.PlayerProfileDto
import com.kbo.summary.core.dto.PlayerRankingDto
import com.kbo.summary.core.dto.PlayerSearchResultDto
import com.kbo.summary.core.dto.PlayerStatDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/players")
class PlayerController(
    private val playerService: PlayerService,
) {
    @GetMapping("/search")
    fun search(@RequestParam("q") keyword: String): ApiResponse<List<PlayerSearchResultDto>> =
        ApiResponse.ok(playerService.searchPlayers(keyword))

    @GetMapping("/rankings/hitter")
    @Cacheable("hitterRankings")
    fun getHitterRankings(@RequestParam type: String): ApiResponse<List<PlayerRankingDto>> {
        // URL 파라미터 H(안타)는 서비스 카테고리 'hits' 로 매핑한다
        val category = if (type.equals("H", ignoreCase = true)) "hits" else type.lowercase()
        return ApiResponse.ok(playerService.getPlayerRankings(category, "hitter"))
    }

    @GetMapping("/rankings/pitcher")
    @Cacheable("pitcherRankings")
    fun getPitcherRankings(@RequestParam type: String): ApiResponse<List<PlayerRankingDto>> =
        ApiResponse.ok(playerService.getPlayerRankings(type.lowercase(), "pitcher"))

    @GetMapping("/{playerId}")
    // birthDate 가 비어있는 stub 응답은 캐시하지 않아 다음 조회에서 lazy crawl 이 다시 시도되도록 한다
    @Cacheable("playerProfile", unless = "#result?.data?.birthDate == null")
    fun getPlayer(@PathVariable playerId: String): ApiResponse<PlayerProfileDto> =
        ApiResponse.ok(playerService.getPlayerProfile(playerId))

    @GetMapping("/{playerId}/stats")
    fun getPlayerStats(@PathVariable playerId: String): ApiResponse<PlayerStatDto> =
        ApiResponse.ok(playerService.getPlayerStats(playerId))

}
