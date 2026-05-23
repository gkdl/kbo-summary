import { Tabs } from "expo-router";
import { useTheme } from "@react-navigation/native";

export default function TabsLayout() {
  const { colors } = useTheme();
  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: colors.primary,
        headerStyle: { backgroundColor: colors.background },
        headerTitleStyle: { color: colors.text },
      }}
    >
      <Tabs.Screen name="index" options={{ title: "홈" }} />
      <Tabs.Screen name="players" options={{ title: "선수" }} />
      <Tabs.Screen name="rankings" options={{ title: "순위" }} />
    </Tabs>
  );
}
