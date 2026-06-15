package com.kbo.summary.core.dto

import java.time.LocalDateTime

data class CreateCommentRequest(
    val content: String,
    // 답글이면 부모 댓글 ID, 최상위 댓글이면 null
    val parentId: Long?,
)

data class CommentDto(
    val commentId: Long,
    val parentId: Long?,
    val content: String,
    val authorId: Long,
    val authorNickname: String,
    val createdAt: LocalDateTime,
    val deleted: Boolean,
    val mine: Boolean,
    // 이 댓글의 답글들 (최상위 댓글에만 채워짐)
    val replies: List<CommentDto> = emptyList(),
)

data class LikeResponse(
    val liked: Boolean,
    val likeCount: Int,
)
