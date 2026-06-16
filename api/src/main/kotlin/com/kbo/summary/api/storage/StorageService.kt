package com.kbo.summary.api.storage

import org.springframework.web.multipart.MultipartFile

/**
 * 이미지 저장소 추상화. 현재는 서버 로컬 디스크(LocalStorageService).
 * 추후 GCS 등으로 교체 시 이 인터페이스 구현만 추가하면 된다.
 */
interface StorageService {
    /** 이미지를 저장하고 공개 접근 경로(예: "/uploads/abc.jpg")를 반환한다. */
    fun store(file: MultipartFile): String

    /** 저장된 경로의 이미지를 삭제한다 (실패해도 예외를 던지지 않음). */
    fun delete(publicPath: String)
}
