package com.kbo.summary.core.dto

import java.time.LocalDateTime

data class CreatePostRequest(
    val teamCode: String,
    val title: String,
    val content: String,
    val imageUrls: List<String> = emptyList(),
)

data class PostListItemDto(
    val postId: Long,
    val teamCode: String,
    val title: String,
    val authorNickname: String,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: LocalDateTime,
    // 목록 썸네일 (첫 번째 이미지). 없으면 null
    val thumbnailUrl: String?,
)

data class PostListDto(
    val items: List<PostListItemDto>,
    val page: Int,
    val hasNext: Boolean,
)

data class PostDetailDto(
    val postId: Long,
    val teamCode: String,
    val title: String,
    val content: String,
    val authorId: Long,
    val authorNickname: String,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: LocalDateTime,
    // 현재 사용자가 작성자인지 (삭제 버튼 노출 등)
    val mine: Boolean,
    // 현재 사용자가 좋아요를 눌렀는지
    val liked: Boolean,
    val imageUrls: List<String> = emptyList(),
)
