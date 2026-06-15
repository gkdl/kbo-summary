package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import java.time.LocalDateTime

enum class PostStatus {
    ACTIVE,
    DELETED,
    HIDDEN,
}

@Entity
@Table(name = "TB_POST")
class Post(
    @Column(name = "MEMBER_ID", nullable = false)
    val memberId: Long,

    // 게시판 식별자 — 어느 구단 게시판에 쓴 글인가 (작성자 응원팀과 무관)
    @Column(name = "TEAM_CODE", length = 10, nullable = false)
    var teamCode: String,

    @Column(name = "TITLE", length = 100, nullable = false)
    var title: String,

    @Lob
    @Column(name = "CONTENT", nullable = false)
    var content: String,

    @Column(name = "VIEW_COUNT", nullable = false)
    var viewCount: Int = 0,

    @Column(name = "LIKE_COUNT", nullable = false)
    var likeCount: Int = 0,

    @Column(name = "COMMENT_COUNT", nullable = false)
    var commentCount: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10, nullable = false)
    var status: PostStatus = PostStatus.ACTIVE,

    @Column(name = "CREATED_AT", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_seq")
    @SequenceGenerator(name = "post_seq", sequenceName = "SEQ_POST", allocationSize = 1)
    @Column(name = "POST_ID")
    val postId: Long? = null,
)
