import { DarkTheme, DefaultTheme, ThemeProvider } from "@react-navigation/native";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Tabs, useRouter } from "expo-router";
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

  // 앱 시작 시 AsyncStorage 값을 Zustand 스토어로 로드한다
  useEffect(() => {
    userPrefs.load().then((prefs) => {
      hydrate({ myTeam: prefs.myTeam, isOnboardingDone: prefs.isOnboardingDone });
    });
  }, [hydrate]);

  // 마이팀이 설정돼 있지 않으면 온보딩 화면으로 보낸다
  useEffect(() => {
    if (isHydrated && !myTeam) {
      router.replace("/onboarding");
    }
  }, [isHydrated, myTeam, router]);

  if (!isHydrated) {
    return null;
  }

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider value={colorScheme === "dark" ? DarkTheme : DefaultTheme}>
        <Tabs screenOptions={{ headerShown: true }}>
          <Tabs.Screen name="index" options={{ title: "경기" }} />
          <Tabs.Screen name="teams" options={{ title: "팀" }} />
          <Tabs.Screen name="players" options={{ title: "선수" }} />
          <Tabs.Screen name="settings" options={{ title: "설정" }} />
          <Tabs.Screen name="onboarding" options={{ href: null }} />
        </Tabs>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
