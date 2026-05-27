#!/bin/sh
set -e

# ============================================================
# Railway / docker run 진입점
# - WALLET_B64 환경변수 (base64 인코딩된 wallet zip) 가 있으면
#   /opt/wallet 으로 풀고 TNS_ADMIN 을 설정한다.
# - 없으면 미리 마운트된 /opt/wallet 을 그대로 사용.
# - 둘 다 없으면 경고만 찍고 진행 (DB 미사용 모드).
# ============================================================

WALLET_DIR=/opt/wallet

if [ -n "$WALLET_B64" ]; then
  echo "[entrypoint] Decoding WALLET_B64 to $WALLET_DIR"
  mkdir -p "$WALLET_DIR"
  TMP_ZIP=$(mktemp /tmp/wallet.XXXXXX.zip)
  printf "%s" "$WALLET_B64" | base64 -d > "$TMP_ZIP"
  unzip -o -q "$TMP_ZIP" -d "$WALLET_DIR"
  rm -f "$TMP_ZIP"
  export TNS_ADMIN="$WALLET_DIR"
  echo "[entrypoint] TNS_ADMIN=$TNS_ADMIN ($(ls $WALLET_DIR | wc -l) files)"
elif [ -d "$WALLET_DIR" ] && [ -f "$WALLET_DIR/tnsnames.ora" ]; then
  echo "[entrypoint] Using pre-mounted wallet at $WALLET_DIR"
  export TNS_ADMIN="$WALLET_DIR"
else
  echo "[entrypoint] WARNING: WALLET_B64 not set and no wallet at $WALLET_DIR."
  echo "[entrypoint] Oracle DB connection will fail unless TNS_ADMIN points elsewhere."
fi

# Spring Boot 컨테이너 환경에서 PID 1 시그널 처리를 위해 exec
exec java -XX:MaxRAMPercentage=75.0 -jar /app/app.jar
