import { Tabs } from "expo-router";
// SDK 56 부터 @react-navigation/bottom-tabs 는 expo-router 내부로 흡수됨.
// 외부 dep 없이 deep import 로 BottomTabBar 사용.
import { BottomTabBar } from "expo-router/build/react-navigation/bottom-tabs";
import { Platform, StyleSheet, View } from "react-native";

import { AdBanner } from "../../components/AdBanner";
import { HomeIcon, PeopleIcon, TrophyIcon } from "../../components/icons/TabIcons";
import { useTheme } from "../../hooks/useTheme";

const ICON_BY_ROUTE: Record<
  string,
  React.ComponentType<{ color: string; size?: number; focused?: boolean }>
> = {
  index: HomeIcon,
  players: PeopleIcon,
  rankings: TrophyIcon,
};

export default function TabsLayout() {
  const { colors } = useTheme();

  return (
    <Tabs
      // 광고를 탭바 바로 위에 고정 — 스크롤과 무관하게 항상 노출.
      // BottomTabBar 는 expo-router 내부에서도 쓰는 컴포넌트로, 기본 탭바를 그대로 렌더링.
      tabBar={(props) => (
        <View style={{ backgroundColor: colors.card }}>
          <AdBanner />
          <BottomTabBar {...props} />
        </View>
      )}
      screenOptions={({ route }) => ({
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.text + "99",
        tabBarShowLabel: true,
        tabBarStyle: {
          backgroundColor: colors.card,
          borderTopColor: colors.border,
          borderTopWidth: StyleSheet.hairlineWidth,
          elevation: 0,
          shadowOpacity: 0,
          height: Platform.OS === "ios" ? 88 : 68,
          paddingBottom: Platform.OS === "ios" ? 28 : 10,
          paddingTop: 8,
        },
        // lineHeight 없이는 한글 라벨의 위/아래가 잘릴 수 있어 명시
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: "600",
          lineHeight: 16,
          marginTop: 3,
          includeFontPadding: false,
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
      <Tabs.Screen name="index" options={{ title: "홈" }} />
      <Tabs.Screen name="players" options={{ title: "선수" }} />
      <Tabs.Screen name="rankings" options={{ title: "순위" }} />
    </Tabs>
  );
}
