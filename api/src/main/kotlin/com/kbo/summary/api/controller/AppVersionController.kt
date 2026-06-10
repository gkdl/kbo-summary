package com.kbo.summary.api.controller

import com.kbo.summary.core.dto.ApiResponse
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 모바일 앱의 강제 업데이트 게이트.
 *
 * 클라이언트가 앱 시작 시 호출해 자신의 nativeBuildVersion(Android versionCode /
 * iOS CFBundleVersion)이 minVersion 미만이면 풀스크린 업데이트 모달을 띄운다.
 * latestVersion 은 "선택 업데이트" UX 에 활용될 수 있다 (현재는 모바일에서 미사용).
 *
 * 값 변경 방법:
 *   - 환경변수: `APP_VERSION_GATE_ANDROID_MIN_VERSION=12`
 *   - 또는 application.yml 의 `app.version-gate.android.min-version`
 */
data class PlatformVersionInfo(
    val minVersion: Int,
    val latestVersion: Int,
    val storeUrl: String,
)

data class AppVersionResponse(
    val android: PlatformVersionInfo,
    val ios: PlatformVersionInfo,
)

@ConfigurationProperties("app.version-gate")
data class AppVersionGateProperties(
    val android: PlatformVersionInfo = PlatformVersionInfo(
        minVersion = 13,
        latestVersion = 13,
        storeUrl = "https://play.google.com/store/apps/details?id=com.kbo.summary",
    ),
    val ios: PlatformVersionInfo = PlatformVersionInfo(
        // iOS 는 아직 미배포라 사실상 게이트 비활성 (모든 빌드 통과)
        minVersion = 1,
        latestVersion = 1,
        storeUrl = "https://apps.apple.com/app/id000000000",
    ),
)

@Configuration
@EnableConfigurationProperties(AppVersionGateProperties::class)
class AppVersionConfig

@RestController
@RequestMapping("/api/app-version")
class AppVersionController(
    private val props: AppVersionGateProperties,
) {
    @GetMapping
    fun get(): ApiResponse<AppVersionResponse> =
        ApiResponse.ok(AppVersionResponse(android = props.android, ios = props.ios))
}
