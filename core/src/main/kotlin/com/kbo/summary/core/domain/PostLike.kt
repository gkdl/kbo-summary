package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Embeddable
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime

@Embeddable
data class PostLikeId(
    @Column(name = "POST_ID")
    val postId: Long = 0,
    @Column(name = "MEMBER_ID")
    val memberId: Long = 0,
) : Serializable

@Entity
@Table(name = "TB_POST_LIKE")
class PostLike(
    @EmbeddedId
    val id: PostLikeId,

    @Column(name = "CREATED_AT", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
