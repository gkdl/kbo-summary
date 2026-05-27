import axios from "axios";

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

apiClient.interceptors.request.use((cfg) => {
  apiDebug.lastUrl = `${cfg.method?.toUpperCase()} ${cfg.baseURL}${cfg.url}`;
  console.log("[api] →", apiDebug.lastUrl);
  return cfg;
});

apiClient.interceptors.response.use(
  (res) => {
    apiDebug.lastStatus = res.status;
    apiDebug.lastError = undefined;
    return res;
  },
  (err) => {
    apiDebug.lastStatus = err?.response?.status;
    apiDebug.lastError = err?.code
      ? `${err.code}: ${err.message}`
      : (err?.message ?? String(err));
    console.warn("[api] ✕", apiDebug.lastUrl, apiDebug.lastError);
    return Promise.reject(err);
  },
);
