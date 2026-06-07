package com.kbo.summary.api.client

import org.springframework.stereotype.Component
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Gemini free-tier 한도 (분당 15회 / 일일 1,000회) 보호용 인메모리 스로틀.
 *
 * 세 가지 가드를 조합한다:
 *  1. 슬라이딩 윈도우 — 분당 12회로 안전마진 두고 자체 제한
 *  2. Per-gameId negative cache — 한 번 실패한 gameId 는 5분간 차단
 *  3. Global pause — 429 받으면 60초간 모든 호출 차단 (한도 회복 대기)
 *
 * 핵심 의도: fallback 응답은 DB·캐시에 저장되지 않으므로, 한도 초과 상황에서
 * 추가 호출이 한도를 재차 갉아먹는 악순환을 막는다.
 *
 * 스레드 안전성: tryAcquire 는 synchronized — 짧은 critical section이라
 * 분당 12회 호출량에서 컨텐션 무시할 수준.
 */
@Component
class GeminiThrottle {

    // 최근 1분간 호출 timestamp (ms)
    private val callTimestamps: ArrayDeque<Long> = ArrayDeque()

    // gameId → 차단 해제 시각(ms). 영구 보관되나 5분 TTL 이라 lazy 정리.
    private val negativeCacheUntil = ConcurrentHashMap<String, Long>()

    // 429 받으면 일정 시간 전체 호출 차단
    private val globalPausedUntilRef = AtomicLong(0L)

    /**
     * Gemini 호출을 시도해도 되는지 판정. true 면 호출 가능(슬롯도 차감됨).
     * false 면 호출하지 말고 즉시 fallback 반환해야 한다.
     */
    @Synchronized
    fun tryAcquire(gameId: String): Boolean {
        val now = System.currentTimeMillis()

        // 1. Global pause
        if (now < globalPausedUntilRef.get()) return false

        // 2. Per-gameId negative cache
        val negUntil = negativeCacheUntil[gameId]
        if (negUntil != null) {
            if (now < negUntil) return false
            negativeCacheUntil.remove(gameId)
        }

        // 3. Sliding window rate limit
        val windowStart = now - WINDOW_MILLIS
        while (callTimestamps.peekFirst()?.let { it < windowStart } == true) {
            callTimestamps.pollFirst()
        }
        if (callTimestamps.size >= MAX_PER_MINUTE) return false

        callTimestamps.addLast(now)
        return true
    }

    /** 429 — 분당/일일 한도 초과. 해당 gameId 차단 + 전체 호출도 잠시 멈춤. */
    fun markRateLimited(gameId: String) {
        val now = System.currentTimeMillis()
        negativeCacheUntil[gameId] = now + NEGATIVE_CACHE_TTL_MILLIS
        val nextGlobalPause = now + GLOBAL_PAUSE_AFTER_429_MILLIS
        // CAS 로 가장 늦은 시각을 채택 (동시 429 시 보호 시간 연장)
        var current: Long
        do {
            current = globalPausedUntilRef.get()
            if (nextGlobalPause <= current) return
        } while (!globalPausedUntilRef.compareAndSet(current, nextGlobalPause))
    }

    /** 한도 외 실패 (404, 5xx, 네트워크 등). 해당 gameId 만 차단. */
    fun markFailure(gameId: String) {
        negativeCacheUntil[gameId] = System.currentTimeMillis() + NEGATIVE_CACHE_TTL_MILLIS
    }

    companion object {
        // free tier 15/min — 안전마진 3
        const val MAX_PER_MINUTE = 12
        const val WINDOW_MILLIS = 60_000L
        const val NEGATIVE_CACHE_TTL_MILLIS = 5 * 60 * 1000L
        const val GLOBAL_PAUSE_AFTER_429_MILLIS = 60_000L
    }
}
