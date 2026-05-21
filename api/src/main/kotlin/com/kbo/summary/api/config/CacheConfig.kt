package com.kbo.summary.api.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        val manager = CaffeineCacheManager()
        CACHE_TTL.forEach { (name, ttl) ->
            manager.registerCustomCache(name, buildCache(ttl))
        }
        return manager
    }

    private fun buildCache(ttl: Duration): Cache<Any, Any> =
        Caffeine.newBuilder()
            .expireAfterWrite(ttl)
            .maximumSize(MAX_ENTRIES)
            .build()

    private companion object {
        const val MAX_ENTRIES = 500L

        // 캐시별 TTL — @Cacheable 의 캐시명과 일치해야 한다
        val CACHE_TTL: Map<String, Duration> = mapOf(
            "standings" to Duration.ofMinutes(10),
            "hitterRankings" to Duration.ofMinutes(30),
            "pitcherRankings" to Duration.ofMinutes(30),
            "gameSummary" to Duration.ofHours(24),
            "teamDetail" to Duration.ofHours(6),
            "playerProfile" to Duration.ofHours(12),
        )
    }
}
