# Oracle ATP Wallet 을 Railway 환경변수용 base64 문자열로 변환.
#
# 사용법:
#   .\scripts\encode-wallet.ps1 -WalletPath "C:\oracle\Wallet_p0mpe7dp2i5vywqf"
#
# 출력:
#   1. scripts\.wallet.b64.txt  (gitignore 됨) — Railway 대시보드에 복붙할 값
#   2. 콘솔에 길이/요약
#
# Railway 설정:
#   Project → Variables → New Variable
#     Name:  WALLET_B64
#     Value: (.wallet.b64.txt 의 전체 내용 한 줄로 붙여넣기)

param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$WalletPath
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -Path $WalletPath -PathType Container)) {
    throw "Wallet 폴더가 없음: $WalletPath"
}

# 상대경로 → 절대경로 (Compress-Archive 가 glob 처리 시 상대경로에서 자주 null 반환)
$absWallet = (Resolve-Path -Path $WalletPath).Path

# tnsnames.ora 가 진짜 wallet 인지 한 번 검증
$requiredFiles = @('tnsnames.ora', 'sqlnet.ora', 'cwallet.sso')
foreach ($f in $requiredFiles) {
    $fp = Join-Path $absWallet $f
    if (-not (Test-Path $fp)) {
        Write-Warning "기대한 파일이 없음: $fp (계속 진행)"
    }
}

# 디렉토리 안의 파일을 명시적으로 수집 (glob 보다 안전)
$items = Get-ChildItem -Path $absWallet -File
if ($items.Count -eq 0) {
    throw "Wallet 폴더가 비어있음: $absWallet"
}

# 임시 zip 생성 → base64 인코딩
$tmpZip = Join-Path $env:TEMP "wallet_$(Get-Date -Format 'yyyyMMddHHmmss').zip"
try {
    Compress-Archive -Path $items.FullName -DestinationPath $tmpZip -Force
    $bytes = [System.IO.File]::ReadAllBytes($tmpZip)
    $b64 = [Convert]::ToBase64String($bytes)
} finally {
    if (Test-Path $tmpZip) { Remove-Item $tmpZip -Force }
}

$outPath = Join-Path $PSScriptRoot '.wallet.b64.txt'
[System.IO.File]::WriteAllText($outPath, $b64)

Write-Host ""
Write-Host "✓ Wallet 인코딩 완료" -ForegroundColor Green
Write-Host "  소스:    $WalletPath"
Write-Host "  출력:    $outPath"
Write-Host "  zip 크기: $($bytes.Length) bytes"
Write-Host "  b64 크기: $($b64.Length) chars"
Write-Host ""
Write-Host "다음 단계:" -ForegroundColor Yellow
Write-Host "  1. Railway 대시보드 → Project → Variables"
Write-Host "  2. New Variable 클릭"
Write-Host "  3. Name = WALLET_B64"
Write-Host "  4. Value 에 위 .wallet.b64.txt 의 전체 내용 복사/붙여넣기"
Write-Host "  5. DB_PASSWORD 변수도 같이 설정 (Oracle SCRIPT 사용자 비밀번호)"
Write-Host ""
Write-Host "주의: .wallet.b64.txt 는 .gitignore 되어 있음. 절대 커밋 금지." -ForegroundColor Red
