package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import java.time.LocalDateTime

enum class CommentStatus {
    ACTIVE,
    DELETED,
    HIDDEN,   // 누적 신고로 자동 숨김
}

@Entity
@Table(name = "TB_COMMENT")
class Comment(
    @Column(name = "POST_ID", nullable = false)
    val postId: Long,

    @Column(name = "MEMBER_ID", nullable = false)
    val memberId: Long,

    // NULL 이면 최상위 댓글, 값 있으면 해당 댓글의 답글
    @Column(name = "PARENT_ID")
    val parentId: Long? = null,

    @Column(name = "CONTENT", length = 1000, nullable = false)
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10, nullable = false)
    var status: CommentStatus = CommentStatus.ACTIVE,

    @Column(name = "CREATED_AT", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq")
    @SequenceGenerator(name = "comment_seq", sequenceName = "SEQ_COMMENT", allocationSize = 1)
    @Column(name = "COMMENT_ID")
    val commentId: Long? = null,
)
