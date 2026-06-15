package com.kbo.summary.api.service

import com.kbo.summary.api.repository.MemberRepository
import com.kbo.summary.api.repository.PostLikeRepository
import com.kbo.summary.api.repository.PostRepository
import com.kbo.summary.core.domain.Post
import com.kbo.summary.core.domain.PostLike
import com.kbo.summary.core.domain.PostLikeId
import com.kbo.summary.core.domain.PostStatus
import com.kbo.summary.core.dto.CreatePostRequest
import com.kbo.summary.core.dto.LikeResponse
import com.kbo.summary.core.dto.PostDetailDto
import com.kbo.summary.core.dto.PostListDto
import com.kbo.summary.core.dto.PostListItemDto
import com.kbo.summary.core.exception.ForbiddenException
import com.kbo.summary.core.exception.InvalidInputException
import com.kbo.summary.core.exception.ResourceNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
    private val postLikeRepository: PostLikeRepository,
) {
    @Transactional(readOnly = true)
    fun list(teamCode: String?, sort: String, page: Int): PostListDto {
        val pageable = pageable(sort, page)
        val team = teamCode?.takeIf { it.isNotBlank() }

        val result = if (team != null) {
            postRepository.findByStatusAndTeamCode(PostStatus.ACTIVE, team, pageable)
        } else {
            postRepository.findByStatus(PostStatus.ACTIVE, pageable)
        }

        // 작성자 닉네임 일괄 조회 (N+1 방지)
        val nicknames = memberRepository.findAllById(result.content.map { it.memberId })
            .associate { it.memberId to it.nickname }

        val items = result.content.map { post ->
            PostListItemDto(
                postId = post.postId!!,
                teamCode = post.teamCode,
                title = post.title,
                authorNickname = nicknames[post.memberId] ?: "알 수 없음",
                viewCount = post.viewCount,
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                createdAt = post.createdAt,
            )
        }
        return PostListDto(items = items, page = page, hasNext = result.hasNext())
    }

    @Transactional
    fun detail(postId: Long, currentMemberId: Long?): PostDetailDto {
        val post = activePost(postId)
        post.viewCount += 1
        val nickname = memberRepository.findByIdOrNull(post.memberId)?.nickname ?: "알 수 없음"
        return PostDetailDto(
            postId = post.postId!!,
            teamCode = post.teamCode,
            title = post.title,
            content = post.content,
            authorId = post.memberId,
            authorNickname = nickname,
            viewCount = post.viewCount,
            likeCount = post.likeCount,
            commentCount = post.commentCount,
            createdAt = post.createdAt,
            mine = currentMemberId != null && currentMemberId == post.memberId,
            liked = currentMemberId != null &&
                postLikeRepository.existsById_PostIdAndId_MemberId(post.postId!!, currentMemberId),
        )
    }

    /** 좋아요 토글 — 이미 눌렀으면 취소, 아니면 추가. 결과 상태와 갱신된 카운트 반환. */
    @Transactional
    fun toggleLike(postId: Long, memberId: Long): LikeResponse {
        val post = activePost(postId)
        val liked: Boolean
        if (postLikeRepository.existsById_PostIdAndId_MemberId(postId, memberId)) {
            postLikeRepository.deleteById_PostIdAndId_MemberId(postId, memberId)
            post.likeCount = (post.likeCount - 1).coerceAtLeast(0)
            liked = false
        } else {
            postLikeRepository.save(PostLike(PostLikeId(postId = postId, memberId = memberId)))
            post.likeCount += 1
            liked = true
        }
        return LikeResponse(liked = liked, likeCount = post.likeCount)
    }

    @Transactional
    fun create(memberId: Long, request: CreatePostRequest): Long {
        val team = request.teamCode.trim()
        val title = request.title.trim()
        val content = request.content.trim()
        if (team.isEmpty()) throw InvalidInputException("게시판(팀)을 선택해주세요")
        if (title.isEmpty()) throw InvalidInputException("제목을 입력해주세요")
        if (content.isEmpty()) throw InvalidInputException("내용을 입력해주세요")
        if (title.length > 100) throw InvalidInputException("제목은 100자 이내로 입력해주세요")

        val post = postRepository.save(
            Post(memberId = memberId, teamCode = team, title = title, content = content),
        )
        return post.postId!!
    }

    @Transactional
    fun delete(postId: Long, memberId: Long) {
        val post = activePost(postId)
        if (post.memberId != memberId) throw ForbiddenException("본인 글만 삭제할 수 있습니다")
        post.status = PostStatus.DELETED
    }

    private fun activePost(postId: Long): Post {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw ResourceNotFoundException("게시글을 찾을 수 없습니다")
        if (post.status != PostStatus.ACTIVE) throw ResourceNotFoundException("게시글을 찾을 수 없습니다")
        return post
    }

    private fun pageable(sort: String, page: Int): Pageable {
        val order = when (sort.lowercase()) {
            "popular" -> Sort.by(
                Sort.Order.desc("likeCount"),
                Sort.Order.desc("viewCount"),
                Sort.Order.desc("createdAt"),
            )
            else -> Sort.by(Sort.Order.desc("createdAt"))
        }
        return PageRequest.of(page.coerceAtLeast(0), PAGE_SIZE, order)
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
