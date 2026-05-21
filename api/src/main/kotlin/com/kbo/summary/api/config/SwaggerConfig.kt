package com.kbo.summary.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun kboOpenApi(): OpenAPI =
        OpenAPI().info(
            Info()
                .title("KBO Summary API")
                .description("KBO 경기·팀·선수·순위 정보를 제공하는 REST API")
                .version("v1"),
        )
}
