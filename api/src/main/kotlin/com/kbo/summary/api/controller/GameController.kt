package com.kbo.summary.api.controller

import com.kbo.summary.api.service.GameService
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.dto.GameDetailDto
import com.kbo.summary.core.dto.GameDto
import com.kbo.summary.core.dto.GameSummaryDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/games")
class GameController(
    private val gameService: GameService,
) {
    @GetMapping
    fun getGames(
        @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") date: LocalDate,
    ): ApiResponse<List<GameDto>> =
        ApiResponse.ok(gameService.getGamesByDate(date))

    @GetMapping("/{gameId}")
    fun getGame(@PathVariable gameId: String): ApiResponse<GameDetailDto> =
        ApiResponse.ok(gameService.getGameDetail(gameId))

    @GetMapping("/{gameId}/summary")
    @Cacheable("gameSummary")
    fun getGameSummary(@PathVariable gameId: String): ApiResponse<GameSummaryDto> =
        ApiResponse.ok(gameService.getGameSummary(gameId))
}
