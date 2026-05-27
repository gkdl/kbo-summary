package com.kbo.summary.api.config

import com.kbo.summary.crawler.client.KboWebClient
import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig {

    // crawler 모듈의 KboWebClient 가 주입받는 단일 WebClient 빈
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
            // KBO .asmx 엔드포인트는 koreabaseball.com Referer 가 없으면 에러 페이지를 반환한다
            .defaultHeader(HttpHeaders.REFERER, "https://www.koreabaseball.com/")
            .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_BYTES) }
            .build()
    }

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) KboSummaryCrawler/1.0"
        const val MAX_IN_MEMORY_BYTES = 8 * 1024 * 1024
    }
}
