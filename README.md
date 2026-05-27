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
- **AI 요약**: Google Gemini (`gemini-2.5-flash-lite`, 무료 티어)
- **Mobile**: React Native 0.76 / Expo SDK 52 / TypeScript / Expo Router
- **배포**: Docker / Railway (백엔드) / Expo EAS (모바일)
- **CI**: GitHub Actions (PR=빌드+린트, main 푸시=Railway 자동 배포)

## 사전 준비

- JDK 17
- Node.js 20 이상 (모바일)
- Docker / Docker Compose (로컬 Oracle + API 실행용)

## 로컬 실행 (Docker Compose 권장)

### 1. 환경 변수

```bash
cp .env.example .env
# .env 의 DB_PASSWORD, GEMINI_API_KEY 채우기 (Gemini 키는 https://aistudio.google.com/apikey 에서 무료 발급)
```

### 2. Oracle + API 동시 기동

```bash
docker compose --env-file .env up --build
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator/health

Oracle 헬스체크가 끝난 뒤 API 가 자동으로 부팅된다 (1~2분 소요).

### 3. 백엔드만 (로컬 JVM)

Docker 로 Oracle 만 띄우고 API 는 IntelliJ/CLI 에서 실행:

```bash
docker compose up -d oracle
./gradlew :api:bootRun
```

### 4. 모바일 (mobile)

```bash
cd mobile
npm install
npx expo start
```

`mobile/.env` 또는 셸 환경에 `EXPO_PUBLIC_API_BASE_URL` 을 지정하면 다른 API 서버를 가리킬 수 있다.

## 환경 변수

| 키                          | 설명                                                  |
| --------------------------- | ----------------------------------------------------- |
| `SPRING_PROFILES_ACTIVE`    | `dev` / `prod`                                        |
| `DB_USERNAME` / `DB_PASSWORD` | Oracle 앱 계정 (compose 가 ORACLE_USER/PW 로 매핑)   |
| `ORACLE_HOST` / `ORACLE_PORT` / `ORACLE_SERVICE` | application-*.yml 의 JDBC URL 변수    |
| `GEMINI_API_KEY`            | Google Gemini API 키 (무료). 비어 있으면 요약은 fallback 문구로 대체 |
| `KBO_CRAWLER_BASE_URL`      | KBO 크롤러 기본 URL (기본값: koreabaseball.com)       |
| `EXPO_PUBLIC_API_BASE_URL`  | 모바일에서 호출할 API 베이스 URL (빌드 시 임베드)     |

## 프로파일

| 파일                   | 용도                                  |
| ---------------------- | ------------------------------------- |
| `application.yml`      | 공통 설정 (JPA, Flyway, Swagger)      |
| `application-dev.yml`  | 로컬 개발 (show-sql, DEBUG 로그)      |
| `application-prod.yml` | 운영 (커넥션 풀 튜닝, WARN 로그)      |

## DB 마이그레이션

Flyway 스크립트는 `crawler/src/main/resources/db/migration`에 `V{버전}__{설명}.sql` 형식으로 추가한다.

## 배포

### 백엔드 (Railway)

루트 `Dockerfile` 로 부트 jar 를 빌드해 배포한다. `railway.toml` 의 `healthcheckPath`
가 `/actuator/health` 를 사용하므로 별도 설정 없이 헬스체크가 동작한다.

```bash
# 최초 1회: Railway 프로젝트 연결
railway link

# 배포
railway up
```

GitHub Actions 가 `main` 브랜치에 push 가 들어오면 `RAILWAY_TOKEN` 시크릿으로
자동 배포한다.

필수 Railway 환경 변수:
- `DB_USERNAME`, `DB_PASSWORD`
- `ORACLE_HOST`, `ORACLE_PORT`, `ORACLE_SERVICE` (Railway Oracle plugin 또는 외부 DB)
- `GEMINI_API_KEY`
- `SPRING_PROFILES_ACTIVE=prod`

### 모바일 (Expo EAS)

```bash
cd mobile
npm install -g eas-cli
eas login
eas build --profile preview --platform android   # 내부 테스트 APK
eas build --profile production --platform all    # 스토어 빌드
```

프로파일별 `EXPO_PUBLIC_API_BASE_URL` 은 `eas.json` 에서 관리한다.

## CI

`.github/workflows/ci.yml` 이 두 job 을 실행한다.

- **backend**: `./gradlew build` (테스트 포함)
- **mobile**: `npm run lint`
- **deploy**: `main` 푸시일 때만 Railway 로 배포

커밋 메시지에 `[skip ci]` 가 들어 있으면 모든 job 을 스킵한다.
