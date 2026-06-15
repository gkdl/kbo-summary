package com.kbo.summary.api.repository

import com.kbo.summary.core.domain.Comment
import com.kbo.summary.core.domain.CommentStatus
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
    // 게시글의 모든 댓글 (삭제 포함) — 생성일 오름차순. 서비스에서 트리로 묶고 삭제 처리한다.
    fun findByPostIdOrderByCreatedAtAsc(postId: Long): List<Comment>

    fun countByPostIdAndStatus(postId: Long, status: CommentStatus): Int
}
