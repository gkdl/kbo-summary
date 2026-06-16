package com.kbo.summary.api.controller

import com.kbo.summary.api.auth.CurrentMember
import com.kbo.summary.api.service.BlockService
import com.kbo.summary.core.dto.ApiResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class BlockRequest(val blockedId: Long)

@RestController
class BlockController(
    private val blockService: BlockService,
) {
    @PostMapping("/api/blocks")
    fun block(@RequestBody request: BlockRequest): ApiResponse<Unit> {
        blockService.block(CurrentMember.id(), request.blockedId)
        return ApiResponse.ok(Unit)
    }

    @DeleteMapping("/api/blocks/{blockedId}")
    fun unblock(@PathVariable blockedId: Long): ApiResponse<Unit> {
        blockService.unblock(CurrentMember.id(), blockedId)
        return ApiResponse.ok(Unit)
    }
}
