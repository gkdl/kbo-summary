package com.kbo.summary.api.service

import com.kbo.summary.api.repository.BlockRepository
import com.kbo.summary.core.domain.Block
import com.kbo.summary.core.domain.BlockId
import com.kbo.summary.core.exception.InvalidInputException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockService(
    private val blockRepository: BlockRepository,
) {
    @Transactional
    fun block(blockerId: Long, blockedId: Long) {
        if (blockerId == blockedId) throw InvalidInputException("자기 자신은 차단할 수 없습니다")
        if (blockRepository.existsById_BlockerIdAndId_BlockedId(blockerId, blockedId)) return
        blockRepository.save(Block(BlockId(blockerId = blockerId, blockedId = blockedId)))
    }

    @Transactional
    fun unblock(blockerId: Long, blockedId: Long) {
        blockRepository.deleteById_BlockerIdAndId_BlockedId(blockerId, blockedId)
    }

    @Transactional(readOnly = true)
    fun blockedIds(blockerId: Long): Set<Long> = blockRepository.findBlockedIds(blockerId).toSet()
}
