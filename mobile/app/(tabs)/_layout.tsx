import { Tabs, useRouter } from "expo-router";
// SDK 56 부터 @react-navigation/bottom-tabs 는 expo-router 내부로 흡수됨.
// 외부 dep 없이 deep import 로 BottomTabBar 사용.
import { BottomTabBar } from "expo-router/build/react-navigation/bottom-tabs";
import { Platform, Pressable, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { AdBanner } from "../../components/AdBanner";
import { HomeIcon, PeopleIcon, TrophyIcon, VideoIcon } from "../../components/icons/TabIcons";
import { useTheme } from "../../hooks/useTheme";

const ICON_BY_ROUTE: Record<
  string,
  React.ComponentType<{ color: string; size?: number; focused?: boolean }>
> = {
  index: HomeIcon,
  highlights: VideoIcon,
  players: PeopleIcon,
  rankings: TrophyIcon,
};

export default function TabsLayout() {
  const { colors } = useTheme();
  const router = useRouter();
  // 시스템 제스처 인디케이터(홈바) 영역만큼 탭바 아래 여백을 확보해 탭 라벨이 가려지지 않게 한다.
  // 커스텀 tabBar 래퍼를 쓰면 expo-router 가 자동으로 safe-area 처리해주지 않으므로 직접 처리.
  const insets = useSafeAreaInsets();

  return (
    <Tabs
      sceneContainerStyle={{ backgroundColor: colors.background }}
      tabBar={(props) => (
        <View style={{ backgroundColor: colors.card, paddingBottom: insets.bottom }}>
          <AdBanner />
          <BottomTabBar {...props} />
        </View>
      )}
      screenOptions={({ route }) => ({
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.subText,
        tabBarShowLabel: true,
        tabBarLabelStyle: { fontSize: 11, fontWeight: "600", marginTop: 2 },
        tabBarStyle: {
          backgroundColor: colors.card,
          borderTopColor: colors.border,
          borderTopWidth: StyleSheet.hairlineWidth,
          elevation: 0,
          shadowOpacity: 0,
          height: Platform.OS === "ios" ? 76 : 60,
          paddingTop: 6,
        },
        tabBarItemStyle: {
          flex: 1,
          justifyContent: "center",
          alignItems: "center",
        },
        tabBarIcon: ({ color, focused }) => {
          const Icon = ICON_BY_ROUTE[route.name] ?? HomeIcon;
          return <Icon color={color} focused={focused} size={22} />;
        },
        headerStyle: {
          backgroundColor: colors.background,
          elevation: 0,
          shadowOpacity: 0,
          borderBottomWidth: StyleSheet.hairlineWidth,
          borderBottomColor: colors.border,
        },
        headerTitleStyle: { color: colors.text, fontSize: 17, fontWeight: "700" },
        headerTitleAlign: "center",
      })}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: "홈",
          // 홈 헤더 우측에 마이팀 변경 진입점 — 온보딩을 건너뛰었거나 팀을 바꾸고 싶을 때
          headerRight: () => (
            <Pressable
              onPress={() => router.push("/settings")}
              accessibilityLabel="설정"
              hitSlop={8}
              style={({ pressed }) => [{ paddingHorizontal: 16, opacity: pressed ? 0.6 : 1 }]}
            >
              <Text style={{ color: colors.primary, fontSize: 14, fontWeight: "600" }}>팀 변경</Text>
            </Pressable>
          ),
        }}
      />
      <Tabs.Screen name="highlights" options={{ title: "하이라이트" }} />
      <Tabs.Screen name="players" options={{ title: "선수" }} />
      <Tabs.Screen name="rankings" options={{ title: "순위" }} />
    </Tabs>
  );
}
