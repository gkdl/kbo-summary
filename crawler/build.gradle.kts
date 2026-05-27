import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    implementation(project(":core"))

    // WebClient/코루틴/Jsoup은 :api 모듈에서 크롤러 빈이 컴포넌트 스캔되므로 api()로 노출한다
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    api("org.jsoup:jsoup:1.18.1")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    implementation("com.github.ben-manes.caffeine:caffeine")

    // Flyway는 부트 가능한 :api 모듈이 자동 설정을 인식하도록 api()로 노출한다
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-oracle")

    runtimeOnly("com.oracle.database.jdbc:ojdbc11")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_17
    }
}

// crawler는 라이브러리 모듈 — 실행 가능한 boot jar가 아닌 일반 jar를 생성한다
tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

tasks.withType<Test> {
    useJUnitPlatform()
}
