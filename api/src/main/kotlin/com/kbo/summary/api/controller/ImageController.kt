package com.kbo.summary.api.controller

import com.kbo.summary.api.auth.CurrentMember
import com.kbo.summary.api.storage.StorageService
import com.kbo.summary.core.dto.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class ImageController(
    private val storageService: StorageService,
) {
    /** 이미지 업로드 — 저장 후 공개 경로 반환. 로그인 필요. */
    @PostMapping("/api/images")
    fun upload(@RequestParam("file") file: MultipartFile): ApiResponse<Map<String, String>> {
        CurrentMember.id() // 인증 확인
        val url = storageService.store(file)
        return ApiResponse.ok(mapOf("url" to url))
    }
}
