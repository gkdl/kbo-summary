import { useState } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { TeamSelector } from "../components/TeamSelector";
import { getTeam } from "../constants/teams";
import { border, opacity, radius, spacing } from "../constants/tokens";
import { useTheme } from "../hooks/useTheme";
import { userPrefs } from "../storage/userPrefs";
import { useAppStore } from "../store/useAppStore";

export default function SettingsScreen() {
  const { colors } = useTheme();
  const myTeam = useAppStore((state) => state.myTeam);
  const setMyTeam = useAppStore((state) => state.setMyTeam);
  const [savedNote, setSavedNote] = useState<string | null>(null);

  const apply = async (teamCode: string | null) => {
    await userPrefs.setMyTeam(teamCode);
    setMyTeam(teamCode);
    const name = teamCode ? getTeam(teamCode)?.name ?? teamCode : null;
    setSavedNote(name ? `${name}(으)로 변경했어요` : "응원팀을 해제했어요");
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={[styles.sectionTitle, { color: colors.text }]}>응원하는 팀</Text>
        <Text style={[styles.sectionDesc, { color: colors.subText }]}>
          선택하면 홈 화면 맨 위에 마이팀 경기를 먼저 보여드려요
        </Text>

        <View style={{ marginTop: spacing.md }}>
          <TeamSelector selectedCode={myTeam} onSelect={(code) => apply(code)} />
        </View>

        <Pressable
          onPress={() => apply(null)}
          disabled={myTeam === null}
          style={({ pressed }) => [
            styles.clearButton,
            { borderColor: colors.border, opacity: myTeam === null ? opacity.disabled : pressed ? opacity.pressed : 1 },
          ]}
        >
          <Text style={[styles.clearLabel, { color: colors.subText }]}>응원팀 해제</Text>
        </Pressable>

        {savedNote ? (
          <View style={[styles.note, { backgroundColor: colors.card, borderColor: colors.primary }]}>
            <Text style={[styles.noteText, { color: colors.primary }]}>✓ {savedNote}</Text>
          </View>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: spacing.lg, gap: spacing.sm },
  sectionTitle: { fontSize: 18, fontWeight: "700" },
  sectionDesc: { fontSize: 13, marginTop: 4, lineHeight: 19 },
  clearButton: {
    marginTop: spacing.lg,
    paddingVertical: 12,
    borderRadius: radius.md,
    borderWidth: border.card,
    alignItems: "center",
  },
  clearLabel: { fontSize: 14, fontWeight: "600" },
  note: {
    marginTop: spacing.lg,
    padding: spacing.md,
    borderRadius: radius.md,
    borderWidth: border.card,
    alignItems: "center",
  },
  noteText: { fontSize: 14, fontWeight: "600" },
});
