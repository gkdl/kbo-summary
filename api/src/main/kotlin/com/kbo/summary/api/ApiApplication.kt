package com.kbo.summary.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// core/crawler 모듈의 빈·엔티티·리포지토리까지 스캔하도록 베이스 패키지를 지정한다
@SpringBootApplication(scanBasePackages = ["com.kbo.summary"])
@EntityScan("com.kbo.summary")
@EnableJpaRepositories("com.kbo.summary")
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
