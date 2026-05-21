import axios from "axios";

// EXPO_PUBLIC_* 환경변수는 Expo가 빌드 시 주입한다. 기본값은 로컬 Spring 서버.
const baseURL = process.env.EXPO_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export const apiClient = axios.create({
  baseURL,
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});
