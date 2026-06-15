package com.kbo.summary.api.repository

import com.kbo.summary.core.domain.PostLike
import com.kbo.summary.core.domain.PostLikeId
import org.springframework.data.jpa.repository.JpaRepository

interface PostLikeRepository : JpaRepository<PostLike, PostLikeId> {
    fun existsById_PostIdAndId_MemberId(postId: Long, memberId: Long): Boolean
    fun deleteById_PostIdAndId_MemberId(postId: Long, memberId: Long)
    fun countById_PostId(postId: Long): Int
}
