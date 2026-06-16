package com.kbo.summary.api.repository

import com.kbo.summary.core.domain.PostImage
import org.springframework.data.jpa.repository.JpaRepository

interface PostImageRepository : JpaRepository<PostImage, Long> {
    fun findByPostIdOrderBySortOrderAsc(postId: Long): List<PostImage>

    // 목록 썸네일용 — 여러 글의 이미지를 한 번에 조회
    fun findByPostIdInOrderBySortOrderAsc(postIds: List<Long>): List<PostImage>
}
