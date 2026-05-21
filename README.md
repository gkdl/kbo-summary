# KBO Summary

KBO(한국 프로야구) 경기 데이터를 수집·요약하여 REST API와 모바일 앱으로 제공하는 프로젝트.

## 모듈 구성

| 모듈      | 설명                                         | 의존             |
| --------- | -------------------------------------------- | ---------------- |
| `core`    | 공통 도메인 / DTO / 예외                     | -                |
| `crawler` | 데이터 수집 (WebClient, Jsoup), JPA, Flyway  | `core`           |
| `api`     | REST API 서버 (Spring MVC, Security, Swagger)| `core`, `crawler`|
| `mobile`  | React Native (Expo) 모바일 앱                | -                |

## 기술 스택

- **Backend**: Spring Boot 3.4.1 / Kotlin 2.1.0 / JDK 17 / Gradle (Kotlin DSL)
- **DB**: Oracle 21c XE / Flyway
- **Mobile**: React Native 0.76 / Expo SDK 52 / TypeScript

## 사전 준비

- JDK 17
- Node.js 18 이상 (모바일)
- Docker (로컬 Oracle 실행용)

## 로컬 실행

### 1. 환경 변수

`.env.example`를 복사해 `.env`를 만들고 값을 채운다.

```bash
cp .env.example .env
```

### 2. Oracle DB (Docker)

```bash
docker run -d --name kbo-oracle \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=kbo \
  -e APP_USER=kbo \
  -e APP_USER_PASSWORD=kbo \
  -v "$(pwd)/oracle_data:/opt/oracle/oradata" \
  gvenzl/oracle-xe:21-slim
```

기동 후 `XEPDB1` PDB에 `kbo` 계정으로 접속할 수 있다.

### 3. 백엔드 (api)

> **최초 1회**: Gradle Wrapper가 없으면 생성한다. IntelliJ에서 `kbo-summary`를
> Gradle 프로젝트로 열면 자동 생성되며, CLI로는 `gradle wrapper --gradle-version 8.12`.

```bash
./gradlew :api:bootRun
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

기본 프로파일은 `dev`. 운영 설정으로 띄우려면 `SPRING_PROFILES_ACTIVE=prod`.

### 4. 전체 빌드 / 테스트

```bash
./gradlew build
```

### 5. 모바일 (mobile)

```bash
cd mobile
npm install
npx expo start
```

의존성 버전 충돌이 있으면 `npx expo install --fix`로 정렬한다.

## 프로파일

| 파일                   | 용도                                  |
| ---------------------- | ------------------------------------- |
| `application.yml`      | 공통 설정 (JPA, Flyway, Swagger)      |
| `application-dev.yml`  | 로컬 개발 (show-sql, DEBUG 로그)      |
| `application-prod.yml` | 운영 (커넥션 풀 튜닝, WARN 로그)      |

## DB 마이그레이션

Flyway 스크립트는 `crawler/src/main/resources/db/migration`에 `V{버전}__{설명}.sql` 형식으로 추가한다.
