import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Stack, useRouter } from "expo-router";
import { useEffect } from "react";
import { Platform, StatusBar, StyleSheet } from "react-native";

import { DebugOverlay } from "../components/DebugOverlay";
import { ForceUpdateScreen } from "../components/ForceUpdateScreen";
import { useAppVersion } from "../hooks/useAppVersion";
import { useTheme } from "../hooks/useTheme";
import { initAds } from "../lib/initAds";
import { userPrefs } from "../storage/userPrefs";
import { useAppStore } from "../store/useAppStore";
import { useAuthStore } from "../store/useAuthStore";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 60_000, retry: 1, refetchOnWindowFocus: false },
  },
});

export default function RootLayout() {
  const router = useRouter();
  const hydrate = useAppStore((state) => state.hydrate);
  const isHydrated = useAppStore((state) => state.isHydrated);
  const myTeam = useAppStore((state) => state.myTeam);
  const isOnboardingDone = useAppStore((state) => state.isOnboardingDone);

  // 앱 시작 시 AsyncStorage 값을 Zustand 스토어로 로드
  useEffect(() => {
    userPrefs.load().then((prefs) => {
      hydrate({ myTeam: prefs.myTeam, isOnboardingDone: prefs.isOnboardingDone });
    });
  }, [hydrate]);

  // 앱 시작 시 SecureStore 의 로그인 토큰 복원
  useEffect(() => {
    void useAuthStore.getState().hydrate();
  }, []);

  // Google AdMob SDK 초기화 (BannerAd 렌더링 전에 반드시 1회 필요)
  // web 번들에서는 initAds.web.ts 의 no-op 가 자동 선택됨
  useEffect(() => {
    initAds();
  }, []);

  // 온보딩이 끝나지 않았고 마이팀도 없으면 온보딩으로
  useEffect(() => {
    if (isHydrated && !myTeam && !isOnboardingDone) {
      router.replace("/onboarding");
    }
  }, [isHydrated, myTeam, isOnboardingDone, router]);

  if (!isHydrated) {
    return null;
  }

  return (
    <QueryClientProvider client={queryClient}>
      <ThemedStack />
      {/* DebugOverlay 는 dev 빌드에서만 표시 (release 에선 자동 제거) */}
      {__DEV__ ? <DebugOverlay /> : null}
    </QueryClientProvider>
  );
}

function ThemedStack() {
  const { colors, dark } = useTheme();
  const versionGate = useAppVersion();

  // 강제 업데이트 게이트 — Stack 마운트 전에 분기해 모든 화면을 차단
  // (네트워크 실패·웹·Expo Go 는 useAppVersion 이 fail-open 처리)
  if (versionGate.needsForceUpdate && versionGate.storeUrl) {
    return (
      <>
        <StatusBar
          barStyle={dark ? "light-content" : "dark-content"}
          backgroundColor={colors.background}
        />
        <ForceUpdateScreen
          storeUrl={versionGate.storeUrl}
          currentVersion={versionGate.currentVersion}
          minVersion={versionGate.minVersion}
        />
      </>
    );
  }

  return (
    <>
      <StatusBar
        barStyle={dark ? "light-content" : "dark-content"}
        backgroundColor={colors.background}
      />
      <Stack
      screenOptions={{
        contentStyle: { backgroundColor: colors.background },
        headerStyle: {
          backgroundColor: colors.background,
          elevation: 0,
          shadowOpacity: 0,
          borderBottomWidth: StyleSheet.hairlineWidth,
          borderBottomColor: colors.border,
        },
        headerTitleStyle: { fontSize: 17, fontWeight: "700", color: colors.text },
        headerTitleAlign: "center",
        headerTintColor: colors.text,
        // iOS 의 "Back" 텍스트 숨김 (Android 에는 영향 없음)
        ...(Platform.OS === "ios" ? { headerBackTitle: "" } : {}),
      }}
    >
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
      <Stack.Screen name="onboarding" options={{ headerShown: false }} />
      <Stack.Screen name="login" options={{ headerShown: false, presentation: "modal" }} />
      <Stack.Screen name="settings" options={{ title: "설정" }} />
      <Stack.Screen name="game/[gameId]" options={{ title: "경기 상세" }} />
      <Stack.Screen name="team/[teamCode]" options={{ title: "팀 상세" }} />
      <Stack.Screen name="player/[playerId]" options={{ title: "선수 상세" }} />
      <Stack.Screen name="post/[postId]" options={{ title: "게시글" }} />
      <Stack.Screen name="post/write" options={{ headerShown: false, presentation: "modal" }} />
    </Stack>
    </>
  );
}
