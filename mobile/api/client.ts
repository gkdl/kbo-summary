import axios, { type AxiosRequestConfig } from "axios";

import { useAuthStore } from "../store/useAuthStore";
import type { TokenResponse } from "../types/auth";

// EXPO_PUBLIC_* 환경변수는 Expo가 빌드 시 주입한다. 기본값은 로컬 Spring 서버.
const baseURL = process.env.EXPO_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

// eslint-disable-next-line import/no-named-as-default-member -- axios.create 는 default export 의 메서드라 named import 불가
export const apiClient = axios.create({
  baseURL,
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});

// 디버그용 — 최근 실패한 요청 정보를 메모리에 저장. <DebugOverlay /> 에서 표시.
type DebugInfo = {
  baseURL: string;
  lastUrl?: string;
  lastError?: string;
  lastStatus?: number;
};
export const apiDebug: DebugInfo = { baseURL };

// 인증 갱신을 시도하지 않는 경로 (이 경로의 401 은 그대로 전달)
const AUTH_FREE_PATHS = ["/api/auth/kakao", "/api/auth/refresh"];

apiClient.interceptors.request.use((cfg) => {
  const token = useAuthStore.getState().accessToken;
  if (token && !AUTH_FREE_PATHS.some((p) => cfg.url?.includes(p))) {
    cfg.headers.Authorization = `Bearer ${token}`;
  }
  apiDebug.lastUrl = `${cfg.method?.toUpperCase()} ${cfg.baseURL}${cfg.url}`;
  console.log("[api] →", apiDebug.lastUrl);
  return cfg;
});

// 401 → refresh 토큰으로 1회 재발급 후 원요청 재시도. 실패하면 로그아웃.
async function tryRefresh(): Promise<string | null> {
  const refreshToken = useAuthStore.getState().refreshToken;
  if (!refreshToken) return null;
  try {
    // 인터셉터 재귀를 피하려고 순수 axios 로 호출
    const res = await axios.post<{ data: TokenResponse }>(
      `${baseURL}/api/auth/refresh`,
      { refreshToken },
      { headers: { "Content-Type": "application/json" }, timeout: 10000 },
    );
    const tokens = res.data.data;
    await useAuthStore.getState().setTokens(tokens.accessToken, tokens.refreshToken);
    return tokens.accessToken;
  } catch {
    await useAuthStore.getState().clearAuth();
    return null;
  }
}

apiClient.interceptors.response.use(
  (res) => {
    apiDebug.lastStatus = res.status;
    apiDebug.lastError = undefined;
    return res;
  },
  async (err) => {
    apiDebug.lastStatus = err?.response?.status;
    apiDebug.lastError = err?.code
      ? `${err.code}: ${err.message}`
      : (err?.message ?? String(err));
    console.warn("[api] ✕", apiDebug.lastUrl, apiDebug.lastError);

    const original = err.config as (AxiosRequestConfig & { _retried?: boolean }) | undefined;
    const status = err?.response?.status;
    const url = original?.url ?? "";
    if (
      status === 401 &&
      original &&
      !original._retried &&
      !AUTH_FREE_PATHS.some((p) => url.includes(p))
    ) {
      original._retried = true;
      const newToken = await tryRefresh();
      if (newToken) {
        original.headers = { ...original.headers, Authorization: `Bearer ${newToken}` };
        return apiClient(original);
      }
    }
    return Promise.reject(err);
  },
);
