package com.kbo.summary.core.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime

@Embeddable
data class BlockId(
    @Column(name = "BLOCKER_ID")
    val blockerId: Long = 0,
    @Column(name = "BLOCKED_ID")
    val blockedId: Long = 0,
) : Serializable

@Entity
@Table(name = "TB_BLOCK")
class Block(
    @EmbeddedId
    val id: BlockId,

    @Column(name = "CREATED_AT", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
