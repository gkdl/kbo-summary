package com.kbo.summary.api.service

import com.kbo.summary.api.repository.CommentRepository
import com.kbo.summary.api.repository.MemberRepository
import com.kbo.summary.api.repository.PostRepository
import com.kbo.summary.core.domain.Comment
import com.kbo.summary.core.domain.CommentStatus
import com.kbo.summary.core.domain.PostStatus
import com.kbo.summary.core.dto.CommentDto
import com.kbo.summary.core.dto.CreateCommentRequest
import com.kbo.summary.core.exception.ForbiddenException
import com.kbo.summary.core.exception.InvalidInputException
import com.kbo.summary.core.exception.ResourceNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
    private val profanityFilter: ProfanityFilter,
    private val blockService: BlockService,
) {
    @Transactional(readOnly = true)
    fun list(postId: Long, currentMemberId: Long?): List<CommentDto> {
        // 내가 차단한 작성자의 댓글은 제외 (해당 댓글이 최상위면 그 답글도 함께 빠진다)
        val blockedIds = currentMemberId?.let { blockService.blockedIds(it) } ?: emptySet()
        val all = commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .filter { it.memberId !in blockedIds }
        if (all.isEmpty()) return emptyList()

        val nicknames = memberRepository.findAllById(all.map { it.memberId })
            .associate { it.memberId to it.nickname }
        val repliesByParent = all.filter { it.parentId != null }.groupBy { it.parentId }

        fun dto(c: Comment, replies: List<CommentDto>): CommentDto {
            val deleted = c.status != CommentStatus.ACTIVE
            return CommentDto(
                commentId = c.commentId!!,
                parentId = c.parentId,
                content = if (deleted) "삭제된 댓글입니다" else c.content,
                authorId = c.memberId,
                authorNickname = if (deleted) "" else (nicknames[c.memberId] ?: "알 수 없음"),
                createdAt = c.createdAt,
                deleted = deleted,
                mine = currentMemberId != null && currentMemberId == c.memberId && !deleted,
                replies = replies,
            )
        }

        // 최상위 댓글만 펼치고, 각 댓글의 ACTIVE 답글을 붙인다.
        // 삭제된 최상위 댓글은 살아있는 답글이 있을 때만 placeholder 로 노출, 없으면 생략.
        return all.filter { it.parentId == null }.mapNotNull { top ->
            val replies = repliesByParent[top.commentId].orEmpty()
                .filter { it.status == CommentStatus.ACTIVE }
                .map { dto(it, emptyList()) }
            when {
                top.status == CommentStatus.ACTIVE -> dto(top, replies)
                replies.isNotEmpty() -> dto(top, replies)
                else -> null
            }
        }
    }

    @Transactional
    fun create(postId: Long, memberId: Long, request: CreateCommentRequest): Long {
        val content = request.content.trim()
        if (content.isEmpty()) throw InvalidInputException("댓글 내용을 입력해주세요")
        if (content.length > 1000) throw InvalidInputException("댓글은 1000자 이내로 입력해주세요")
        if (profanityFilter.containsProfanity(content)) {
            throw InvalidInputException("부적절한 표현이 포함되어 있어 등록할 수 없습니다")
        }

        val post = postRepository.findByIdOrNull(postId)?.takeIf { it.status == PostStatus.ACTIVE }
            ?: throw ResourceNotFoundException("게시글을 찾을 수 없습니다")

        // 답글이면 부모를 검증. 답글에 답글을 달면 최상위 댓글로 평탄화(1단계 유지).
        val parentId = request.parentId?.let { pid ->
            val parent = commentRepository.findByIdOrNull(pid)
                ?: throw InvalidInputException("원 댓글을 찾을 수 없습니다")
            if (parent.postId != postId) throw InvalidInputException("잘못된 댓글입니다")
            parent.parentId ?: parent.commentId
        }

        val saved = commentRepository.save(
            Comment(postId = postId, memberId = memberId, parentId = parentId, content = content),
        )
        post.commentCount = commentRepository.countByPostIdAndStatus(postId, CommentStatus.ACTIVE)
        return saved.commentId!!
    }

    @Transactional
    fun delete(commentId: Long, memberId: Long) {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?.takeIf { it.status == CommentStatus.ACTIVE }
            ?: throw ResourceNotFoundException("댓글을 찾을 수 없습니다")
        if (comment.memberId != memberId) throw ForbiddenException("본인 댓글만 삭제할 수 있습니다")
        comment.status = CommentStatus.DELETED
        postRepository.findByIdOrNull(comment.postId)?.let {
            it.commentCount = commentRepository.countByPostIdAndStatus(comment.postId, CommentStatus.ACTIVE)
        }
    }
}
