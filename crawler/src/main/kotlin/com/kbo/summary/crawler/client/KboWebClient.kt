package com.kbo.summary.crawler.client

import com.kbo.summary.core.exception.CrawlerException
import io.netty.channel.ChannelOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class KboWebClientConfig {

    @Bean
    fun kboCrawlerWebClient(
        @Value("\${kbo.crawler.base-url:https://www.koreabaseball.com}") baseUrl: String,
    ): WebClient {
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(KboWebClient.TIMEOUT_MILLIS))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, KboWebClient.TIMEOUT_MILLIS.toInt())

        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .defaultHeader(HttpHeaders.ACCEPT, "application/json, text/plain, */*")
            .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9")
            .defaultHeader("X-Requested-With", "XMLHttpRequest")
            .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_BYTES) }
            .build()
    }

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) KboSummaryCrawler/1.0"
        const val MAX_IN_MEMORY_BYTES = 8 * 1024 * 1024
    }
}

@Component
class KboWebClient(
    private val webClient: WebClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun get(path: String): String =
        execute("GET $path") {
            webClient.get()
                .uri(path)
                .retrieve()
                .awaitBody<String>()
        }

    suspend fun post(path: String, params: Map<String, String>): String =
        execute("POST $path") {
            val formData = LinkedMultiValueMap<String, String>()
            params.forEach { (key, value) -> formData.add(key, value) }
            webClient.post()
                .uri(path)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .awaitBody<String>()
        }

    // 요청 간 500ms 간격을 두고, 실패 시 최대 3회 지수 백오프(1s → 2s → 4s)로 재시도한다
    private suspend fun execute(label: String, request: suspend () -> String): String {
        var lastError: Throwable? = null
        for (attempt in 0..MAX_RETRIES) {
            if (attempt > 0) {
                val backoffMillis = BACKOFF_BASE_MILLIS shl (attempt - 1)
                log.warn("KBO 요청 재시도 {}/{} ({}) — {}ms 대기", attempt, MAX_RETRIES, label, backoffMillis)
                delay(backoffMillis)
            }
            delay(REQUEST_DELAY_MILLIS)
            try {
                return request()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
                log.warn("KBO 요청 실패 ({}): {}", label, e.message)
            }
        }
        throw CrawlerException("KBO 요청 실패: $label ($MAX_RETRIES회 재시도 초과)", lastError)
    }

    companion object {
        const val MAX_RETRIES = 3
        const val TIMEOUT_MILLIS = 10_000L
        const val REQUEST_DELAY_MILLIS = 500L
        const val BACKOFF_BASE_MILLIS = 1_000L
    }
}
