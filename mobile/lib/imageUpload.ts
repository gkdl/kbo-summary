import * as ImagePicker from "expo-image-picker";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";

const baseURL = process.env.EXPO_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

/** 서버가 돌려준 상대 경로(/uploads/..)를 전체 URL 로 변환 */
export function imageFullUrl(path: string): string {
  if (path.startsWith("http")) return path;
  return `${baseURL}${path}`;
}

/** 갤러리에서 이미지 선택 — 최대 limit 장. 로컬 asset URI 배열 반환 */
export async function pickImages(limit: number): Promise<string[]> {
  const perm = await ImagePicker.requestMediaLibraryPermissionsAsync();
  if (!perm.granted) return [];
  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    allowsMultipleSelection: true,
    selectionLimit: limit,
    quality: 0.8,
  });
  if (result.canceled) return [];
  return result.assets.map((a) => a.uri);
}

/** 로컬 이미지를 서버에 업로드하고 공개 경로(/uploads/..)를 반환 */
export async function uploadImage(uri: string): Promise<string> {
  const form = new FormData();
  // RN 의 FormData 파일 형식
  form.append("file", {
    uri,
    name: `photo_${Date.now()}.jpg`,
    type: "image/jpeg",
  } as unknown as Blob);

  const res = await apiClient.post<ApiResponse<{ url: string }>>("/api/images", form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  const url = res.data.data?.url;
  if (!url) throw new Error("이미지 업로드 실패");
  return url;
}
