import { DarkTheme, DefaultTheme, ThemeProvider } from "@react-navigation/native";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Stack, useRouter } from "expo-router";
import { useEffect } from "react";
import { useColorScheme } from "react-native";

import { userPrefs } from "../storage/userPrefs";
import { useAppStore } from "../store/useAppStore";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 60_000, retry: 1, refetchOnWindowFocus: false },
  },
});

export default function RootLayout() {
  const colorScheme = useColorScheme();
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
      <ThemeProvider value={colorScheme === "dark" ? DarkTheme : DefaultTheme}>
        <Stack>
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen name="onboarding" options={{ headerShown: false }} />
          <Stack.Screen name="game/[gameId]" options={{ title: "경기 상세" }} />
          <Stack.Screen name="team/[teamCode]" options={{ title: "팀 상세" }} />
          <Stack.Screen name="player/[playerId]" options={{ title: "선수 상세" }} />
        </Stack>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
