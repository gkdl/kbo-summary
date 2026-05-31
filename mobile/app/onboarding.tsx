import { useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { useTheme } from "../hooks/useTheme";

import { TeamSelector } from "../components/TeamSelector";
import { userPrefs } from "../storage/userPrefs";
import { useAppStore } from "../store/useAppStore";

export default function OnboardingScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const [selected, setSelected] = useState<string | null>(null);
  const setMyTeam = useAppStore((state) => state.setMyTeam);
  const setOnboardingDone = useAppStore((state) => state.setOnboardingDone);

  const finish = async (teamCode: string | null) => {
    if (teamCode !== null) {
      await userPrefs.setMyTeam(teamCode);
      setMyTeam(teamCode);
    }
    await userPrefs.setOnboardingDone(true);
    setOnboardingDone(true);
    router.replace("/");
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.content}>
        <Text style={[styles.title, { color: colors.text }]}>응원하는 팀을 선택하세요</Text>
        <Text style={[styles.subtitle, { color: colors.subText }]}>
          홈 화면에서 마이팀 경기를 우선 표시합니다
        </Text>
        <View style={{ marginTop: 24 }}>
          <TeamSelector selectedCode={selected} onSelect={setSelected} />
        </View>
      </View>

      <View style={styles.actions}>
        <Pressable
          onPress={() => finish(null)}
          style={({ pressed }) => [
            styles.button,
            styles.secondary,
            { borderColor: colors.border, opacity: pressed ? 0.6 : 1 },
          ]}
        >
          <Text style={[styles.buttonText, { color: colors.subText }]}>건너뛰기</Text>
        </Pressable>
        <Pressable
          disabled={selected === null}
          onPress={() => selected !== null && finish(selected)}
          style={({ pressed }) => [
            styles.button,
            {
              backgroundColor: selected !== null ? colors.primary : colors.border,
              opacity: pressed && selected !== null ? 0.7 : 1,
            },
          ]}
        >
          <Text style={[styles.buttonText, { color: "#FFFFFF" }]}>시작하기</Text>
        </Pressable>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: "space-between" },
  content: { padding: 24 },
  title: { fontSize: 24, fontWeight: "700" },
  subtitle: { fontSize: 14, marginTop: 8 },
  actions: { flexDirection: "row", padding: 16, gap: 12 },
  button: { flex: 1, paddingVertical: 14, borderRadius: 8, alignItems: "center" },
  secondary: { borderWidth: 1 },
  buttonText: { fontSize: 16, fontWeight: "600" },
});
