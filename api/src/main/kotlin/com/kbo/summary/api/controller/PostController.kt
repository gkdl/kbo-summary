package com.kbo.summary.api.controller

import com.kbo.summary.api.auth.CurrentMember
import com.kbo.summary.api.service.PostService
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.dto.CreatePostRequest
import com.kbo.summary.core.dto.LikeResponse
import com.kbo.summary.core.dto.PostDetailDto
import com.kbo.summary.core.dto.PostListDto
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) team: String?,
        @RequestParam(defaultValue = "latest") sort: String,
        @RequestParam(defaultValue = "0") page: Int,
    ): ApiResponse<PostListDto> =
        ApiResponse.ok(postService.list(team, sort, page, CurrentMember.idOrNull()))

    @GetMapping("/{postId}")
    fun detail(@PathVariable postId: Long): ApiResponse<PostDetailDto> =
        ApiResponse.ok(postService.detail(postId, CurrentMember.idOrNull()))

    @PostMapping
    fun create(@RequestBody request: CreatePostRequest): ApiResponse<Map<String, Long>> {
        val postId = postService.create(CurrentMember.id(), request)
        return ApiResponse.ok(mapOf("postId" to postId))
    }

    @DeleteMapping("/{postId}")
    fun delete(@PathVariable postId: Long): ApiResponse<Unit> {
        postService.delete(postId, CurrentMember.id())
        return ApiResponse.ok(Unit)
    }

    @PostMapping("/{postId}/like")
    fun toggleLike(@PathVariable postId: Long): ApiResponse<LikeResponse> =
        ApiResponse.ok(postService.toggleLike(postId, CurrentMember.id()))
}
