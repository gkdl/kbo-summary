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
    // 공통 모듈의 기반 의존성은 api()로 노출해 crawler/api 모듈이 상속받게 한다
    api("org.springframework.boot:spring-boot-starter")
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JPA 엔티티가 core 모듈에 위치하므로 매핑 애노테이션 API가 필요하다
    api("jakarta.persistence:jakarta.persistence-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.13")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_17
    }
}

// core는 라이브러리 모듈 — 실행 가능한 boot jar가 아닌 일반 jar를 생성한다
tasks.bootJar { enabled = false }
tasks.jar { enabled = true }

tasks.withType<Test> {
    useJUnitPlatform()
}
