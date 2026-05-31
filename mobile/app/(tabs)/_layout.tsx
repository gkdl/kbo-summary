import { Tabs } from "expo-router";
// SDK 56 부터 @react-navigation/bottom-tabs 는 expo-router 내부로 흡수됨.
// 외부 dep 없이 deep import 로 BottomTabBar 사용.
import { BottomTabBar } from "expo-router/build/react-navigation/bottom-tabs";
import { Platform, StyleSheet, View } from "react-native";

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

  return (
    <Tabs
      sceneContainerStyle={{ backgroundColor: colors.background }}
      tabBar={(props) => (
        <View style={{ backgroundColor: colors.card }}>
          <AdBanner />
          <BottomTabBar {...props} />
        </View>
      )}
      screenOptions={({ route }) => ({
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.subText,
        tabBarShowLabel: false,
        tabBarStyle: {
          backgroundColor: colors.card,
          borderTopColor: colors.border,
          borderTopWidth: StyleSheet.hairlineWidth,
          elevation: 0,
          shadowOpacity: 0,
          height: Platform.OS === "ios" ? 72 : 56,
        },
        tabBarItemStyle: {
          flex: 1,
          justifyContent: "center",
          alignItems: "center",
          paddingVertical: 0,
          marginVertical: 0,
        },
        tabBarIcon: ({ color, focused }) => {
          const Icon = ICON_BY_ROUTE[route.name] ?? HomeIcon;
          return (
            <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
              <Icon color={color} focused={focused} size={22} />
            </View>
          );
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
      <Tabs.Screen name="highlights" options={{ title: "하이라이트" }} />
      <Tabs.Screen name="players" options={{ title: "선수" }} />
      <Tabs.Screen name="rankings" options={{ title: "순위" }} />
    </Tabs>
  );
}
