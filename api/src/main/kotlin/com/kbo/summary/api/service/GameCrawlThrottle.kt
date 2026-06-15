package com.kbo.summary.api.service

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 조회 경로의 populate-on-demand 크롤 연타 방지용 인메모리 throttle.
 *
 * 조회 시 크롤은 "DB 에 아직 없는 날짜/경기" 를 채울 때만 발생하지만,
 * 경기가 없는 날짜(휴식일)나 아직 KBO 가 데이터를 발행하지 않은 경기는
 * 크롤해도 DB 가 계속 비어 있어 매 요청마다 다시 크롤하게 된다.
 * 같은 key 는 TTL 안에서 1회만 크롤하도록 막아 KBO 과다호출을 방지한다.
 *
 * 오늘 경기의 라이브 갱신은 스케줄러(KboCrawlerScheduler)가 담당하므로
 * 조회 경로는 빈 데이터 보충 외에는 크롤하지 않는다.
 */
@Component
class GameCrawlThrottle {

    private val lastCrawlAt = ConcurrentHashMap<String, Long>()

    /**
     * key 에 대해 크롤을 시도해도 되는지 판정. true 면 즉시 타임스탬프를 갱신한다(=슬롯 점유).
     * TTL 안에 이미 크롤했으면 false 를 반환하니 호출자는 크롤을 건너뛰고 DB 결과만 쓰면 된다.
     */
    fun tryAcquire(key: String, ttlMillis: Long = DEFAULT_TTL_MILLIS): Boolean {
        val now = System.currentTimeMillis()
        var acquired = false
        // compute 는 해당 버킷을 잠그고 실행되므로 check-and-set 이 원자적이다
        lastCrawlAt.compute(key) { _, prev ->
            if (prev == null || now - prev >= ttlMillis) {
                acquired = true
                now
            } else {
                prev
            }
        }
        return acquired
    }

    companion object {
        const val DEFAULT_TTL_MILLIS = 60_000L
    }
}
