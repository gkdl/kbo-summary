import { useQuery } from "@tanstack/react-query";
import Constants from "expo-constants";
import { Platform } from "react-native";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { AppVersionResponse, PlatformVersionInfo } from "../types/appVersion";

export interface AppVersionGate {
  // 강제 업데이트 필요 여부 — true 면 ForceUpdateScreen 을 막아야 한다
  needsForceUpdate: boolean;
  storeUrl: string | null;
  currentVersion: number | null;
  minVersion: number | null;
  // 네트워크/플랫폼 조회 결과를 알 수 없는 단계
  isResolving: boolean;
}

/**
 * 서버에서 받은 minVersion 과 현재 빌드의 nativeBuildVersion 을 비교해 강제 업데이트 필요 여부를 반환.
 *
 * 정책:
 *  - 웹/Expo Go(빌드 버전 미상)는 게이트 비활성화 — 항상 통과
 *  - 네트워크 실패도 게이트 비활성화 — 서버 장애가 모든 사용자를 차단하면 안 된다 (fail-open)
 *  - 로딩 중에는 isResolving = true 로 알려 호출자가 스플래시를 유지할 수 있게 함
 */
export function useAppVersion(): AppVersionGate {
  const platformKey: keyof AppVersionResponse | null =
    Platform.OS === "android" ? "android" : Platform.OS === "ios" ? "ios" : null;

  // Constants.nativeBuildVersion: Android=versionCode(string), iOS=CFBundleVersion(string)
  // 웹·Expo Go 환경에서는 null/undefined
  const currentVersion = parseBuildVersion(Constants.nativeBuildVersion);

  const query = useQuery({
    queryKey: ["appVersion"],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<AppVersionResponse>>(
        "/api/app-version",
      );
      return res.data.data ?? null;
    },
    // 한 세션 안에서는 다시 조회할 필요 없음
    staleTime: Infinity,
    retry: 1,
  });

  if (platformKey == null || currentVersion == null) {
    return {
      needsForceUpdate: false,
      storeUrl: null,
      currentVersion,
      minVersion: null,
      isResolving: false,
    };
  }

  if (query.isLoading) {
    return {
      needsForceUpdate: false,
      storeUrl: null,
      currentVersion,
      minVersion: null,
      isResolving: true,
    };
  }

  const platformInfo: PlatformVersionInfo | undefined = query.data?.[platformKey];
  if (!platformInfo) {
    // fail-open
    return {
      needsForceUpdate: false,
      storeUrl: null,
      currentVersion,
      minVersion: null,
      isResolving: false,
    };
  }

  return {
    needsForceUpdate: currentVersion < platformInfo.minVersion,
    storeUrl: platformInfo.storeUrl,
    currentVersion,
    minVersion: platformInfo.minVersion,
    isResolving: false,
  };
}

function parseBuildVersion(raw: string | null | undefined): number | null {
  if (raw == null) return null;
  const n = parseInt(raw, 10);
  return Number.isFinite(n) ? n : null;
}
