# KBO Summary 앱 아이콘 생성 스크립트
# - icon.png             : 1024x1024  (배경 + 디자인)
# - adaptive-icon.png    : 1024x1024  (안전영역 중앙, 투명 배경)
# - favicon.png          : 48x48      (웹용)
# splash.png 은 별도 유지.

Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = 'Stop'

$assetsDir        = Join-Path $PSScriptRoot '..\assets'
$iconPath         = Join-Path $assetsDir 'icon.png'
$adaptiveIconPath = Join-Path $assetsDir 'adaptive-icon.png'
$faviconPath      = Join-Path $assetsDir 'favicon.png'

function Draw-StitchTicks {
    param(
        $Graphics, $Pen, $Cx, $Cy, $Rx, $Ry,
        [single]$StartAngleDeg, [single]$SweepAngleDeg,
        [int]$TickCount, [single]$TickLength
    )
    for ($i = 0; $i -lt $TickCount; $i++) {
        $t   = $i / [double]($TickCount - 1)
        $a   = $StartAngleDeg + $SweepAngleDeg * $t
        $rad = $a * [Math]::PI / 180.0
        $px  = $Cx + $Rx * [Math]::Cos($rad)
        $py  = $Cy + $Ry * [Math]::Sin($rad)
        # 접선 방향에 수직으로 짧게
        $tx  = -[Math]::Sin($rad)
        $ty  =  [Math]::Cos($rad)
        $half = $TickLength * 0.5
        $Graphics.DrawLine(
            $Pen,
            [single]($px - $tx * $half), [single]($py - $ty * $half),
            [single]($px + $tx * $half), [single]($py + $ty * $half)
        )
    }
}

function New-IconBitmap {
    param(
        [int]$Size,
        [bool]$Transparent = $false
    )

    # 색상
    $brandDark   = [System.Drawing.Color]::FromArgb(255, 15, 10, 48)
    $brandMid    = [System.Drawing.Color]::FromArgb(255, 26, 23, 72)
    $brandLight  = [System.Drawing.Color]::FromArgb(255, 56, 48, 130)
    $stitchRed   = [System.Drawing.Color]::FromArgb(255, 200, 35, 45)

    $bmp = [System.Drawing.Bitmap]::new($Size, $Size)
    $g   = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode     = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

    if (-not $Transparent) {
        # 라운드 사각형 배경
        $radius = [int]($Size * 0.22)
        $d = $radius * 2
        $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
        $path.AddArc(0, 0, $d, $d, 180, 90) | Out-Null
        $path.AddArc($Size - $d, 0, $d, $d, 270, 90) | Out-Null
        $path.AddArc($Size - $d, $Size - $d, $d, $d, 0, 90) | Out-Null
        $path.AddArc(0, $Size - $d, $d, $d, 90, 90) | Out-Null
        $path.CloseFigure()

        # 베이스: 브랜드 컬러
        $baseBrush = [System.Drawing.SolidBrush]::new($brandMid)
        $g.FillPath($baseBrush, $path)
        $baseBrush.Dispose()

        # 라디얼 하이라이트 (상단 중앙)
        $g.SetClip($path)
        $hSize = [int]($Size * 1.4)
        $hX    = [int](($Size - $hSize) / 2)
        $hY    = [int](-$Size * 0.55)
        $hPath = [System.Drawing.Drawing2D.GraphicsPath]::new()
        $hPath.AddEllipse($hX, $hY, $hSize, $hSize)
        $hBrush = [System.Drawing.Drawing2D.PathGradientBrush]::new($hPath)
        $hBrush.CenterColor    = [System.Drawing.Color]::FromArgb(180, $brandLight.R, $brandLight.G, $brandLight.B)
        $hBrush.SurroundColors = @([System.Drawing.Color]::FromArgb(0, $brandDark.R, $brandDark.G, $brandDark.B))
        $g.FillPath($hBrush, $hPath)
        $hBrush.Dispose()
        $hPath.Dispose()

        $g.ResetClip()
        $path.Dispose()
    }

    # --- 야구공 (텍스트 제거 후 중앙 정렬, 살짝 작게) ---
    $ballSize = [single]($Size * 0.50)
    $ballX    = [single](($Size - $ballSize) * 0.5)
    $ballY    = [single](($Size - $ballSize) * 0.5)

    # 야구공 그림자
    $shOff = [single]($Size * 0.012)
    $shadowBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(70, 0, 0, 0))
    $g.FillEllipse($shadowBrush, [single]($ballX + $shOff), [single]($ballY + $shOff * 2), $ballSize, $ballSize)
    $shadowBrush.Dispose()

    # 야구공 본체 (그라데이션)
    $ballRect = [System.Drawing.RectangleF]::new($ballX, $ballY, $ballSize, $ballSize)
    $ballGrad = [System.Drawing.Drawing2D.LinearGradientBrush]::new(
        $ballRect,
        [System.Drawing.Color]::FromArgb(255, 255, 255, 255),
        [System.Drawing.Color]::FromArgb(255, 222, 222, 215),
        [single]135.0
    )
    $g.FillEllipse($ballGrad, $ballRect)
    $ballGrad.Dispose()

    # 야구공 외곽선 (Pen 은 Brush 기반으로 생성 — PS5.1 의 Color/Brush 오버로드 모호성 회피)
    $ballPenBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(60, 0, 0, 0))
    $ballPen = [System.Drawing.Pen]::new([System.Drawing.Brush]$ballPenBrush, [single]($Size * 0.004))
    $g.DrawEllipse($ballPen, $ballRect)
    $ballPen.Dispose()
    $ballPenBrush.Dispose()

    # --- 빨간 실밥 (양쪽 곡선) ---
    $stitchBrush = [System.Drawing.SolidBrush]::new($stitchRed)
    $stitchPen = [System.Drawing.Pen]::new([System.Drawing.Brush]$stitchBrush, [single]($Size * 0.018))
    $stitchPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $stitchPen.EndCap   = [System.Drawing.Drawing2D.LineCap]::Round

    # 왼쪽 곡선
    $leftX = [single]($ballX - $ballSize * 0.18)
    $leftY = [single]($ballY + $ballSize * 0.05)
    $leftW = [single]($ballSize * 0.55)
    $leftH = [single]($ballSize * 0.9)
    $leftRect = [System.Drawing.RectangleF]::new($leftX, $leftY, $leftW, $leftH)
    $g.DrawArc($stitchPen, $leftRect, [single](-70), [single]140)

    # 오른쪽 곡선
    $rightX = [single]($ballX + $ballSize * 0.63)
    $rightY = [single]($ballY + $ballSize * 0.05)
    $rightW = [single]($ballSize * 0.55)
    $rightH = [single]($ballSize * 0.9)
    $rightRect = [System.Drawing.RectangleF]::new($rightX, $rightY, $rightW, $rightH)
    $g.DrawArc($stitchPen, $rightRect, [single]110, [single]140)
    $stitchPen.Dispose()
    $stitchBrush.Dispose()

    # 실밥 짧은 빗금
    $tickBrush = [System.Drawing.SolidBrush]::new($stitchRed)
    $tickPen = [System.Drawing.Pen]::new([System.Drawing.Brush]$tickBrush, [single]($Size * 0.012))
    $tickPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $tickPen.EndCap   = [System.Drawing.Drawing2D.LineCap]::Round

    $leftCx = $leftX + $leftW * 0.5
    $leftCy = $leftY + $leftH * 0.5
    Draw-StitchTicks $g $tickPen $leftCx $leftCy ($leftW * 0.5) ($leftH * 0.5) ([single](-70)) ([single]140) 9 ([single]($Size * 0.055))

    $rightCx = $rightX + $rightW * 0.5
    $rightCy = $rightY + $rightH * 0.5
    Draw-StitchTicks $g $tickPen $rightCx $rightCy ($rightW * 0.5) ($rightH * 0.5) ([single]110) ([single]140) 9 ([single]($Size * 0.055))
    $tickPen.Dispose()
    $tickBrush.Dispose()

    $g.Dispose()
    return $bmp
}

# 1024x1024 풀 아이콘
Write-Host 'Generating icon.png (1024x1024)...'
$icon = New-IconBitmap -Size 1024 -Transparent:$false
$icon.Save($iconPath, [System.Drawing.Imaging.ImageFormat]::Png)
$icon.Dispose()

# 1024x1024 adaptive icon foreground (투명 배경)
Write-Host 'Generating adaptive-icon.png (1024x1024, transparent)...'
$adaptive = New-IconBitmap -Size 1024 -Transparent:$true
$adaptive.Save($adaptiveIconPath, [System.Drawing.Imaging.ImageFormat]::Png)
$adaptive.Dispose()

# 48x48 favicon
Write-Host 'Generating favicon.png (48x48)...'
$fav = New-IconBitmap -Size 48 -Transparent:$false
$fav.Save($faviconPath, [System.Drawing.Imaging.ImageFormat]::Png)
$fav.Dispose()

Write-Host ''
Write-Host 'Done.'
Get-ChildItem $assetsDir -Filter *.png | Format-Table Name, Length, LastWriteTime -AutoSize
