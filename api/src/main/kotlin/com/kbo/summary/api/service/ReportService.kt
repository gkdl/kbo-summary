package com.kbo.summary.api.service

import com.kbo.summary.api.repository.CommentRepository
import com.kbo.summary.api.repository.PostRepository
import com.kbo.summary.api.repository.ReportRepository
import com.kbo.summary.core.domain.CommentStatus
import com.kbo.summary.core.domain.PostStatus
import com.kbo.summary.core.domain.Report
import com.kbo.summary.core.domain.ReportReason
import com.kbo.summary.core.domain.ReportTargetType
import com.kbo.summary.core.dto.CreateReportRequest
import com.kbo.summary.core.exception.InvalidInputException
import com.kbo.summary.core.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /** 신고 접수 — 중복신고는 무시. 누적 신고가 임계치에 도달하면 대상을 자동 숨김 처리. */
    @Transactional
    fun report(reporterId: Long, request: CreateReportRequest) {
        val targetType = parseTargetType(request.targetType)
        val reason = parseReason(request.reason)
        validateTargetExists(targetType, request.targetId)

        // 이미 신고했으면 조용히 종료 (중복 카운트 방지)
        if (reportRepository.existsByTargetTypeAndTargetIdAndReporterId(
                targetType, request.targetId, reporterId,
            )
        ) {
            return
        }

        reportRepository.save(
            Report(
                targetType = targetType,
                targetId = request.targetId,
                reporterId = reporterId,
                reason = reason,
            ),
        )

        val count = reportRepository.countByTargetTypeAndTargetId(targetType, request.targetId)
        if (count >= AUTO_HIDE_THRESHOLD) {
            autoHide(targetType, request.targetId)
        }
    }

    private fun autoHide(targetType: ReportTargetType, targetId: Long) {
        when (targetType) {
            ReportTargetType.POST ->
                postRepository.findByIdOrNull(targetId)
                    ?.takeIf { it.status == PostStatus.ACTIVE }
                    ?.let {
                        it.status = PostStatus.HIDDEN
                        log.warn("게시글 자동 숨김 (누적 신고): postId={}", targetId)
                    }
            ReportTargetType.COMMENT ->
                commentRepository.findByIdOrNull(targetId)
                    ?.takeIf { it.status == CommentStatus.ACTIVE }
                    ?.let {
                        it.status = CommentStatus.HIDDEN
                        log.warn("댓글 자동 숨김 (누적 신고): commentId={}", targetId)
                    }
        }
    }

    private fun validateTargetExists(targetType: ReportTargetType, targetId: Long) {
        val exists = when (targetType) {
            ReportTargetType.POST -> postRepository.findByIdOrNull(targetId) != null
            ReportTargetType.COMMENT -> commentRepository.findByIdOrNull(targetId) != null
        }
        if (!exists) throw ResourceNotFoundException("신고 대상을 찾을 수 없습니다")
    }

    private fun parseTargetType(value: String): ReportTargetType =
        runCatching { ReportTargetType.valueOf(value.uppercase()) }
            .getOrElse { throw InvalidInputException("올바르지 않은 신고 대상입니다") }

    private fun parseReason(value: String): ReportReason =
        runCatching { ReportReason.valueOf(value.uppercase()) }
            .getOrElse { throw InvalidInputException("올바르지 않은 신고 사유입니다") }

    private companion object {
        const val AUTO_HIDE_THRESHOLD = 5
    }
}
