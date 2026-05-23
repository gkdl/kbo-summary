# syntax=docker/dockerfile:1.7

# ===== Build stage =====
# 프로젝트 toolchain 이 JDK 17 이므로 이미지도 17 사용 (Spring Boot 3.4 / Kotlin 2.1)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# 의존성 캐시 최적화: 빌드 스크립트를 먼저 복사
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle/ gradle/
COPY core/build.gradle.kts core/
COPY crawler/build.gradle.kts crawler/
COPY api/build.gradle.kts api/

RUN chmod +x gradlew

# 의존성 사전 다운로드 (소스 변경에 캐시 영향 최소화)
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# 소스 복사 후 부트 jar 생성
COPY core/src core/src
COPY crawler/src crawler/src
COPY api/src api/src
COPY api/libs api/libs

RUN ./gradlew :api:bootJar --no-daemon

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# 부트 jar 만 가져오면 충분 (의존성은 jar 내부에 포함됨)
COPY --from=build /workspace/api/build/libs/*.jar app.jar

EXPOSE 8080

# Spring Boot 컨테이너 환경에서 PID 1 시그널 처리를 위해 직접 java 호출
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
