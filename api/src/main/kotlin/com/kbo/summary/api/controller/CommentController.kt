package com.kbo.summary.api.controller

import com.kbo.summary.api.auth.CurrentMember
import com.kbo.summary.api.service.CommentService
import com.kbo.summary.core.dto.ApiResponse
import com.kbo.summary.core.dto.CommentDto
import com.kbo.summary.core.dto.CreateCommentRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentController(
    private val commentService: CommentService,
) {
    @GetMapping("/api/posts/{postId}/comments")
    fun list(@PathVariable postId: Long): ApiResponse<List<CommentDto>> =
        ApiResponse.ok(commentService.list(postId, CurrentMember.idOrNull()))

    @PostMapping("/api/posts/{postId}/comments")
    fun create(
        @PathVariable postId: Long,
        @RequestBody request: CreateCommentRequest,
    ): ApiResponse<Map<String, Long>> {
        val commentId = commentService.create(postId, CurrentMember.id(), request)
        return ApiResponse.ok(mapOf("commentId" to commentId))
    }

    @DeleteMapping("/api/comments/{commentId}")
    fun delete(@PathVariable commentId: Long): ApiResponse<Unit> {
        commentService.delete(commentId, CurrentMember.id())
        return ApiResponse.ok(Unit)
    }
}
