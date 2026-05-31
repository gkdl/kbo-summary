#!/bin/bash
set -e

cd ~/kbo-summary

echo "=== git pull ==="
git fetch origin main
git reset --hard origin/main

echo "=== Docker 이미지 빌드 ==="
docker build -t kbo-api .

echo "=== 기존 컨테이너 교체 ==="
docker stop kbo-api 2>/dev/null || true
docker rm kbo-api 2>/dev/null || true

echo "=== 컨테이너 실행 ==="
docker run -d \
  --name kbo-api \
  --env-file .env \
  -e JAVA_TOOL_OPTIONS="-Xmx500m -XX:MaxRAMPercentage=50" \
  -p 8080:8080 \
  --restart unless-stopped \
  --memory=700m \
  kbo-api

echo "=== 완료 ==="
docker ps | grep kbo-api
