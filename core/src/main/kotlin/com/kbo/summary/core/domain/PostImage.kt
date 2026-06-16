package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(name = "TB_POST_IMAGE")
class PostImage(
    @Column(name = "POST_ID", nullable = false)
    val postId: Long,

    @Column(name = "URL", length = 300, nullable = false)
    val url: String,

    @Column(name = "SORT_ORDER", nullable = false)
    val sortOrder: Int = 0,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_image_seq")
    @SequenceGenerator(name = "post_image_seq", sequenceName = "SEQ_POST_IMAGE", allocationSize = 1)
    @Column(name = "IMAGE_ID")
    val imageId: Long? = null,
)
