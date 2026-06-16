package com.kbo.summary.api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

/**
 * 업로드된 이미지를 정적 파일로 서빙한다.
 * 공개 경로(app.upload.public-path, 기본 "/uploads") 하위 요청을 로컬 업로드 디렉터리로 매핑한다.
 */
@Configuration
class StaticResourceConfig(
    @Value("\${app.upload.dir:./uploads}") private val uploadDir: String,
    @Value("\${app.upload.public-path:/uploads}") private val publicPath: String,
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString()
        registry.addResourceHandler("$publicPath/**")
            .addResourceLocations(location)
    }
}
