package com.kbo.summary.api.storage

import com.kbo.summary.core.exception.InvalidInputException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

/**
 * 서버 로컬 디스크 저장 구현.
 * - 허용 형식: jpeg / png (서버에서 jpeg 로 재인코딩)
 * - 긴 변이 MAX_DIMENSION 을 넘으면 비율 유지하며 축소 (용량 절감)
 * - 파일명은 UUID 로 생성해 경로 조작·충돌 방지
 *
 * 업로드 디렉터리는 app.upload.dir (기본 ./uploads), 공개 경로 프리픽스는 app.upload.public-path.
 */
@Service
class LocalStorageService(
    @Value("\${app.upload.dir:./uploads}") private val uploadDir: String,
    @Value("\${app.upload.public-path:/uploads}") private val publicPath: String,
) : StorageService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val root: Path = Paths.get(uploadDir).toAbsolutePath().normalize()

    init {
        Files.createDirectories(root)
        log.info("이미지 업로드 디렉터리: {}", root)
    }

    override fun store(file: MultipartFile): String {
        if (file.isEmpty) throw InvalidInputException("빈 파일입니다")
        val contentType = file.contentType ?: ""
        if (contentType !in ALLOWED_TYPES) {
            throw InvalidInputException("JPG 또는 PNG 이미지만 업로드할 수 있습니다")
        }
        if (file.size > MAX_FILE_SIZE) {
            throw InvalidInputException("이미지는 ${MAX_FILE_SIZE / (1024 * 1024)}MB 이하만 업로드할 수 있습니다")
        }

        val image = runCatching { ImageIO.read(file.inputStream) }.getOrNull()
            ?: throw InvalidInputException("이미지를 읽을 수 없습니다")
        val resized = downscale(image)

        val filename = "${UUID.randomUUID()}.jpg"
        val target = root.resolve(filename).normalize()
        if (!target.startsWith(root)) throw InvalidInputException("잘못된 파일 경로입니다")

        Files.write(target, encodeJpeg(toRgb(resized), JPEG_QUALITY))
        return "$publicPath/$filename"
    }

    // JPEG 품질을 명시해 용량을 예측 가능하게 한다 (ImageIO 기본값은 플랫폼마다 달라짐).
    private fun encodeJpeg(image: BufferedImage, quality: Float): ByteArray {
        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
        val param = writer.defaultWriteParam.apply {
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionQuality = quality
        }
        val out = ByteArrayOutputStream()
        ImageIO.createImageOutputStream(out).use { ios ->
            writer.output = ios
            writer.write(null, IIOImage(image, null, null), param)
        }
        writer.dispose()
        return out.toByteArray()
    }

    override fun delete(publicPath: String) {
        runCatching {
            val name = publicPath.substringAfterLast('/')
            val target = root.resolve(name).normalize()
            if (target.startsWith(root)) Files.deleteIfExists(target)
        }.onFailure { log.warn("이미지 삭제 실패 ({}): {}", publicPath, it.message) }
    }

    private fun downscale(image: BufferedImage): BufferedImage {
        val longEdge = maxOf(image.width, image.height)
        if (longEdge <= MAX_DIMENSION) return image
        val scale = MAX_DIMENSION.toDouble() / longEdge
        val w = (image.width * scale).toInt().coerceAtLeast(1)
        val h = (image.height * scale).toInt().coerceAtLeast(1)
        val scaled = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        scaled.createGraphics().apply {
            drawImage(image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH), 0, 0, null)
            dispose()
        }
        return scaled
    }

    // PNG(투명) → JPEG 변환 시 알파 채널 제거 (검은 배경 방지: 흰 배경으로)
    private fun toRgb(image: BufferedImage): BufferedImage {
        if (image.type == BufferedImage.TYPE_INT_RGB) return image
        val rgb = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        rgb.createGraphics().apply {
            color = java.awt.Color.WHITE
            fillRect(0, 0, image.width, image.height)
            drawImage(image, 0, 0, null)
            dispose()
        }
        return rgb
    }

    private companion object {
        val ALLOWED_TYPES = setOf("image/jpeg", "image/jpg", "image/png")
        const val MAX_FILE_SIZE = 8L * 1024 * 1024  // 8MB (업로드 원본 한도)
        const val MAX_DIMENSION = 1280              // 긴 변 최대 px
        const val JPEG_QUALITY = 0.8f               // 저장 품질 (용량/화질 균형)
    }
}
