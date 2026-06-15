import { login as kakaoSdkLogin, logout as kakaoSdkLogout } from "@react-native-seoul/kakao-login";

import { apiClient } from "../api/client";
import type { TokenResponse } from "../types/auth";

interface ApiEnvelope<T> {
  success: boolean;
  data: T | null;
}

/**
 * 카카오 네이티브 로그인 → 카카오 access token 획득 → 백엔드 교환 → 우리 TokenResponse 반환.
 * 백엔드가 카카오 토큰을 검증하고 회원 upsert 후 JWT 를 발급한다.
 */
export async function loginWithKakao(): Promise<TokenResponse> {
  const kakaoToken = await kakaoSdkLogin();
  const res = await apiClient.post<ApiEnvelope<TokenResponse>>("/api/auth/kakao", {
    kakaoAccessToken: kakaoToken.accessToken,
  });
  if (!res.data.data) throw new Error("로그인 응답이 비어 있습니다");
  return res.data.data;
}

/** 카카오 세션 로그아웃 (우리 토큰 폐기는 별도) */
export async function logoutKakao(): Promise<void> {
  try {
    await kakaoSdkLogout();
  } catch {
    // 카카오 세션이 이미 없을 수 있음 — 무시
  }
}
