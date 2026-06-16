package com.kbo.summary.api.repository

import com.kbo.summary.core.domain.Block
import com.kbo.summary.core.domain.BlockId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BlockRepository : JpaRepository<Block, BlockId> {
    fun existsById_BlockerIdAndId_BlockedId(blockerId: Long, blockedId: Long): Boolean
    fun deleteById_BlockerIdAndId_BlockedId(blockerId: Long, blockedId: Long)

    // 차단자가 차단한 회원 ID 목록 (피드/댓글 필터링용)
    @Query("select b.id.blockedId from Block b where b.id.blockerId = :blockerId")
    fun findBlockedIds(blockerId: Long): List<Long>
}
