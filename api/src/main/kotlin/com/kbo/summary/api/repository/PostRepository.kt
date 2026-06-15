package com.kbo.summary.api.repository

import com.kbo.summary.core.domain.Post
import com.kbo.summary.core.domain.PostStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long> {
    // 특정 구단 게시판
    fun findByStatusAndTeamCode(
        status: PostStatus,
        teamCode: String,
        pageable: Pageable,
    ): Page<Post>

    // 전체(모든 구단) — 미선택 시
    fun findByStatus(status: PostStatus, pageable: Pageable): Page<Post>
}
