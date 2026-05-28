package com.kbo.summary.api.controller

import com.kbo.summary.api.service.GameService
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.dto.GameDetailDto
import com.kbo.summary.core.dto.GameDto
import com.kbo.summary.core.dto.GameHighlightDto
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
    // Gemini 429/네트워크 실패로 받은 fallback 메시지는 캐싱하지 않아 다음 호출에서 재시도되게 한다
    @Cacheable("gameSummary", unless = "#result?.data?.summary == '요약을 불러올 수 없습니다'")
    fun getGameSummary(@PathVariable gameId: String): ApiResponse<GameSummaryDto> =
        ApiResponse.ok(gameService.getGameSummary(gameId))

    /**
     * 날짜별 하이라이트 목록. 종료된 경기 중 YouTube 영상이 있는 것만.
     * KBO 호출이 게임 수만큼 일어나므로 캐시 적용.
     */
    @GetMapping("/highlights")
    @Cacheable("highlightsByDate")
    fun getHighlightsByDate(
        @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") date: LocalDate,
    ): ApiResponse<List<GameHighlightDto>> =
        ApiResponse.ok(gameService.getHighlightsByDate(date))
}
