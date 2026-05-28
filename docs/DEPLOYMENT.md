# KBO Summary 배포 가이드

GCP Compute Engine VM 에 Spring Boot API 를 Docker 로 배포하는 절차와
자주 쓰는 명령어 모음. 모바일(EAS) 배포는 별도 문서 참고.

---

## 0. 인프라 개요

```
┌──────────────────┐       ┌──────────────────────────┐       ┌─────────────────┐
│  모바일 (Expo)    │ HTTP  │  GCP VM e2-micro          │  TNS  │  Oracle ATP     │
│  EAS 빌드된 APK   │ ───►  │  Docker: kbo-summary-api  │ ───►  │  (Wallet mTLS)  │
│                  │ 8080  │  us-west1-a               │       │                 │
└──────────────────┘       └──────────────────────────┘       └─────────────────┘
```

- **GCP VM**: `instance-20260527-113725` (zone `us-west1-a`), 외부 IP `104.198.2.246`
- **VM 사용자**: `lim` (OS Login 으로 자동 생성)
- **Spring 컨테이너**: `kbo-api`, 호스트 `8080` 포트 노출
- **Wallet**: VM 의 `~/kbo-summary/.env` 안에 `WALLET_B64` 로 임베디드.
  Docker entrypoint 가 시작 시 `/opt/wallet` 으로 디코딩 + `TNS_ADMIN` 설정.

---

## 1. 사전 준비 (PC 측, 1회만)

### gcloud CLI 설치
```powershell
# 공식 인스톨러 다운로드 + 실행
(New-Object Net.WebClient).DownloadFile(
    "https://dl.google.com/dl/cloudsdk/channels/rapid/GoogleCloudSDKInstaller.exe",
    "$env:TEMP\GoogleCloudSDKInstaller.exe"
)
& $env:TEMP\GoogleCloudSDKInstaller.exe
```
설치 마지막에 `gcloud init` 자동 실행 → 구글 로그인 → 프로젝트 선택.

### 프로젝트 / zone 기본값 설정
```powershell
gcloud config set project kbo-project-497505
gcloud config set compute/zone us-west1-a
```
이후 명령에서 `--project` / `--zone` 매번 안 써도 됨.

---

## 2. VM 접속

```powershell
gcloud compute ssh instance-20260527-113725 --zone=us-west1-a
```

gcloud 가 SSH 키 (`~/.ssh/google_compute_engine`) 를 자동 생성/등록하고
OS Login 으로 접속까지 처리. 기존 native ssh + 메타데이터 키 방식은 OS Login 에서
무시되므로 **항상 `gcloud compute ssh` 사용**.

빠져나올 땐 `exit`.

---

## 3. 코드 가져오기 (VM 안)

```bash
git clone https://github.com/gkdl/kbo-summary
cd kbo-summary
```

업데이트할 땐 `git pull` 만.

---

## 4. Wallet → base64 → .env 만들기

### PC 에서 base64 인코딩
PowerShell:
```powershell
cd F:\claude\kbo-summary
$zip = "$env:TEMP\wallet.zip"
$files = Get-ChildItem -Path .\Wallet -File
Compress-Archive -Path $files.FullName -DestinationPath $zip -Force
$bytes = [System.IO.File]::ReadAllBytes($zip)
$b64 = [Convert]::ToBase64String($bytes)
Remove-Item $zip -Force
[System.IO.File]::WriteAllText("$pwd\scripts\.wallet.b64.txt", $b64)
"zip $($bytes.Length)B, b64 $($b64.Length) chars → scripts\.wallet.b64.txt"
```

### .env.vm 조립 (PC 에서)
```powershell
$walletB64 = Get-Content .\scripts\.wallet.b64.txt -Raw
$envContent = @"
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:oracle:thin:@p0mpe7dp2i5vywqf_tp
DB_USERNAME=SCRIPT
DB_PASSWORD=실제비밀번호
GEMINI_API_KEY=실제Gemini키
WALLET_B64=$walletB64
"@
[System.IO.File]::WriteAllText("$pwd\.env.vm", $envContent)
"size: $((Get-Item .env.vm).Length) bytes"
```

> `.env.vm`, `.wallet.b64.txt` 모두 `.gitignore` 에 잡혀있음 (절대 커밋 X).

### VM 으로 업로드
```powershell
# 절대경로로 명확하게
gcloud compute scp .\.env.vm instance-20260527-113725:/home/lim/.env.vm --zone=us-west1-a
```

> `:~` 끝부분은 PuTTY/pscp 가 잘못 처리하는 경우가 있어 절대경로 권장.
> 본인 user 가 `lim` 인지 확인: VM 안에서 `whoami`.

### VM 안에서 정리
```bash
# 점(.) 으로 시작하는 숨김파일이라 ls 에 안 보임 → ls -la 로 확인
ls -la ~/.env.vm

# kbo-summary 폴더 안 .env 로 이동
mv ~/.env.vm ~/kbo-summary/.env

# 확인
head -3 ~/kbo-summary/.env       # 첫 3줄만
wc -c ~/kbo-summary/.env         # 약 34000+ bytes
```

---

## 5. 초기 셋업 (VM 1회)

VM 에 처음 접속한 직후 한 번만 실행:

```bash
# 시스템 업데이트
sudo apt update && sudo apt upgrade -y
sudo apt install -y git unzip curl

# Swap 2GB (e2-micro 의 1GB RAM 보강 — 빌드/부팅 중 OOM 방지)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER

# 그룹 권한 반영 위해 한 번 로그아웃 후 재접속
exit
```

다시 접속 후 `docker ps` 가 sudo 없이 동작하면 정상.

---

## 6. 빌드 + 실행

### 처음 실행
```bash
cd ~/kbo-summary
docker build -t kbo-api .

docker run -d \
  --name kbo-api \
  --env-file .env \
  -e TZ=Asia/Seoul \
  -e JAVA_TOOL_OPTIONS="-Xmx500m -XX:MaxRAMPercentage=50 -Duser.timezone=Asia/Seoul" \
  -p 8080:8080 \
  --restart unless-stopped \
  --memory=700m \
  kbo-api

docker logs -f kbo-api
```

정상 로그:
```
[entrypoint] Decoding WALLET_B64 to /opt/wallet
[entrypoint] TNS_ADMIN=/opt/wallet (11 files)
Started KboSummaryApiApplication in N.NN seconds
Tomcat started on port 8080
```

### 빠른 재배포 스크립트
`~/deploy.sh`:
```bash
#!/bin/bash
set -e
cd ~/kbo-summary
git pull
docker build -t kbo-api .
docker stop kbo-api 2>/dev/null || true
docker rm kbo-api 2>/dev/null || true
docker run -d \
  --name kbo-api \
  --env-file .env \
  -e JAVA_TOOL_OPTIONS="-Xmx500m -XX:MaxRAMPercentage=50" \
  -p 8080:8080 \
  --restart unless-stopped \
  --memory=700m \
  kbo-api
docker logs -f kbo-api
```
```bash
chmod +x ~/deploy.sh
```

이후엔 `~/deploy.sh` 한 줄로 끝.

---

## 7. GCP 방화벽 (PC 측, 1회만)

VM 외부에서 8080 으로 접근하려면 방화벽 규칙 필요:
```powershell
gcloud compute firewall-rules create allow-spring-8080 \
  --allow=tcp:8080 \
  --source-ranges=0.0.0.0/0 \
  --description="KBO Summary API"
```

테스트:
```powershell
curl http://104.198.2.246:8080/actuator/health
# → {"status":"UP"}
```

---

## 8. 일상 운영 명령 (자주 씀)

### 상태
```bash
docker ps                          # 실행 중인 컨테이너
docker ps -a                       # 멈춘 것까지
docker logs --tail 50 kbo-api      # 최근 50줄
docker logs -f kbo-api             # 실시간 (Ctrl+C 로 종료)
docker stats kbo-api --no-stream   # CPU/RAM 사용량
free -h                            # VM 의 RAM/swap 상태
df -h /                            # 디스크 잔량
```

### 재시작 / 갱신
```bash
docker restart kbo-api             # 그냥 재시작 (코드 변경 X)
~/deploy.sh                        # git pull + 재빌드 + 재실행
```

### env 만 바꿀 때
```bash
nano ~/kbo-summary/.env            # 값 수정 후 Ctrl+O, Enter, Ctrl+X
docker stop kbo-api && docker rm kbo-api
docker run -d --name kbo-api --env-file .env \
  -e JAVA_TOOL_OPTIONS="-Xmx500m -XX:MaxRAMPercentage=50" \
  -p 8080:8080 --restart unless-stopped --memory=700m kbo-api
```

### 완전 청소
```bash
docker stop kbo-api && docker rm kbo-api
docker rmi kbo-api
docker system prune -af            # 안 쓰는 이미지/캐시 다 삭제 (디스크 회복)
```

---

## 9. 트러블슈팅

| 증상 | 원인 / 해결 |
|---|---|
| `Permission denied (publickey)` (native ssh) | OS Login 활성화. **`gcloud compute ssh` 사용**. |
| `Could not fetch resource: ... not found` | VM 이름/zone 오타. `gcloud compute instances list` 로 정확한 값 확인. |
| `pscp: unable to open ~/...: no such file or directory` | 대상 디렉토리 없음. 먼저 `git clone` 으로 디렉토리 생성하거나 절대경로 사용. |
| SCP 성공인데 VM 에서 `ls` 에 안 보임 | 점(`.`) 으로 시작하는 숨김파일. `ls -la` 로 확인. |
| `failed to read dockerfile: ... no such file` | `docker build` 를 `~/kbo-summary` 안에서 실행해야 함. `cd ~/kbo-summary` 먼저. |
| `[entrypoint] WARNING: WALLET_B64 not set` | `.env` 에 `WALLET_B64=` 값이 비었거나 `--env-file .env` 누락. `grep WALLET_B64 .env \| wc -c` 로 길이 확인 (34000+). |
| `ORA-01017: invalid username/password` | `.env` 의 `DB_PASSWORD` 가 Oracle 의 현재 비밀번호와 불일치. 특수문자에 따옴표 필요할 수 있음. |
| `IO Error: Network Adapter could not establish` | Wallet 디코딩 실패. entrypoint 로그에 `TNS_ADMIN=/opt/wallet (N files)` 줄 확인. |
| `Bind for 0.0.0.0:8080 failed: port is already allocated` | 이전 컨테이너 잔존. `docker ps -a` 후 `docker rm -f kbo-api`. |
| `java.lang.OutOfMemoryError` (빌드 또는 부팅 중) | 1GB RAM 부족. `free -h` 로 swap 활성화 확인. 안 되어있으면 [§5](#5-초기-셋업-vm-1회) 의 swap 설정. |
| 모바일 앱에서 `ERR_NETWORK` / `Cleartext not permitted` | Android cleartext HTTP 차단. `mobile/app.json` 에 `expo-build-properties` 플러그인이 `usesCleartextTraffic: true` 로 설정돼있는지 확인 후 mobile 재빌드. |
| VM 재시작 후 외부 IP 변경 | DHCP 라 기본은 비고정. 정적 IP 잡으려면 GCP Console → VPC 네트워크 → IP 주소 → 외부 IP 예약. |

---

## 10. Always Free 한도 (GCP 비용 0 유지 조건)

| 리소스 | 한도 | 본 프로젝트 사용 |
|---|---|---|
| VM | e2-micro 1대, us-west1/central1/east1 중 1곳 | us-west1-a ✓ |
| Persistent Disk | Standard 30GB-month | 10GB ✓ |
| Outbound 트래픽 | 북미발 1GB/월 (중국/호주 제외) | 모바일 트래픽 작음 ✓ |
| Cloud Logging | 50GB/월 | 무시 가능 ✓ |

⚠ 다음 행동은 즉시 과금:
- e2-micro → e2-small 등 업그레이드
- 두 번째 VM 생성
- 한국/일본 등 비미국 리전 이동
- SSD 디스크 사용
- 30GB 초과 디스크

알림 받고 싶으면 결제 예산 설정:
```powershell
# GCP Console → 결제 → 예산 및 알림 → 예산 만들기 → $1 한도 → 이메일
```

---

## 11. 보안 체크리스트

- [ ] `Wallet/` 폴더 프로젝트 외부 (`C:\oracle\...`) 로 이동 + 백업
- [ ] `.env.vm`, `scripts/.wallet.b64.txt` 는 임시 산출물 → 사용 후 삭제 OK
- [ ] DB 비밀번호는 운영용/개발용 분리, 평문 yml 에 박지 않기
- [ ] `application*.yml` 에서 비밀번호는 항상 `${DB_PASSWORD}` 형태
- [ ] git push 전 `git status` 로 `.env` 류 안 올라가는지 확인
- [ ] 비밀번호/키 노출 의심 시 즉시 로테이션 (Oracle 콘솔, Gemini API Studio)
- [ ] HTTPS — 도메인 + Caddy/nginx 로 Let's Encrypt 권장 (현재 HTTP)

---

## 12. 모바일 앱과의 연계

`mobile/eas.json` 의 환경별 API URL:
```json
{
  "build": {
    "development": { "env": { "EXPO_PUBLIC_API_BASE_URL": "http://172.30.1.22:8080" } },
    "preview":     { "env": { "EXPO_PUBLIC_API_BASE_URL": "http://104.198.2.246:8080" } },
    "production":  { "env": { "EXPO_PUBLIC_API_BASE_URL": "https://api.kbo-summary.com" } }
  }
}
```

VM IP 가 바뀌면 위 값 갱신 후 EAS 재빌드:
```powershell
cd F:\claude\kbo-summary\mobile
eas build --profile preview --platform android
```

운영(production)은 도메인 + HTTPS 권장. 정적 IP 안 잡으면 VM 재시작 때마다 IP 변경됨 → 모바일 재빌드 필요.

---

## 13. 향후 자동화 옵션

### GitHub Actions → VM 자동 배포
`.github/workflows/deploy.yml`:
```yaml
name: Deploy to GCP VM
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.GCP_HOST }}
          username: ${{ secrets.GCP_USER }}
          key: ${{ secrets.GCP_SSH_KEY }}
          script: ~/deploy.sh
```

GitHub Secrets:
- `GCP_HOST` = `104.198.2.246`
- `GCP_USER` = `lim`
- `GCP_SSH_KEY` = `Get-Content $HOME\.ssh\google_compute_engine` 의 전체 내용

`git push origin main` 만 하면 VM 에서 자동으로 git pull + 재빌드.

---

## 14. 빠른 참조 — 자주 쓰는 명령 TOP 10

```powershell
# PC (Windows)
gcloud compute ssh instance-20260527-113725 --zone=us-west1-a    # VM 접속
gcloud compute scp <local> instance-20260527-113725:/home/lim/   # 파일 업로드
gcloud compute instances list                                     # VM 정보 확인
```

```bash
# VM (Linux)
docker ps                          # 컨테이너 상태
docker logs --tail 50 kbo-api      # 최근 로그
docker logs -f kbo-api             # 실시간 로그
docker restart kbo-api             # 단순 재시작
~/deploy.sh                        # git pull + 재빌드 + 재실행
nano ~/kbo-summary/.env            # 환경변수 편집
free -h                            # 메모리 / swap 상태
```
