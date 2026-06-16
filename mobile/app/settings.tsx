import { useState } from "react";
import { Alert, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useRouter } from "expo-router";

import { TeamSelector } from "../components/TeamSelector";
import { getTeam } from "../constants/teams";
import { border, opacity, radius, spacing } from "../constants/tokens";
import { useAuth } from "../hooks/useAuth";
import { useTheme } from "../hooks/useTheme";
import { userPrefs } from "../storage/userPrefs";
import { useAppStore } from "../store/useAppStore";

export default function SettingsScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { member, isAuthed, logout, withdraw } = useAuth();
  const myTeam = useAppStore((state) => state.myTeam);
  const setMyTeam = useAppStore((state) => state.setMyTeam);
  const [savedNote, setSavedNote] = useState<string | null>(null);

  const onLogout = () =>
    Alert.alert("로그아웃", "로그아웃 하시겠어요?", [
      { text: "취소", style: "cancel" },
      { text: "로그아웃", style: "destructive", onPress: () => void logout() },
    ]);

  const onWithdraw = () =>
    Alert.alert("회원 탈퇴", "탈퇴하면 작성한 글은 '탈퇴한 회원'으로 표시됩니다. 계속할까요?", [
      { text: "취소", style: "cancel" },
      { text: "탈퇴", style: "destructive", onPress: () => void withdraw() },
    ]);

  const apply = async (teamCode: string | null) => {
    await userPrefs.setMyTeam(teamCode);
    setMyTeam(teamCode);
    const name = teamCode ? getTeam(teamCode)?.name ?? teamCode : null;
    setSavedNote(name ? `${name}(으)로 변경했어요` : "응원팀을 해제했어요");
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={[styles.sectionTitle, { color: colors.text }]}>계정</Text>
        {isAuthed ? (
          <View style={{ marginTop: spacing.sm, gap: spacing.sm }}>
            <View style={[styles.accountRow, { backgroundColor: colors.card, borderColor: colors.border }]}>
              <View style={styles.avatar}>
                <Text style={styles.avatarText}>{(member?.nickname ?? "?").slice(0, 1)}</Text>
              </View>
              <View style={{ flex: 1 }}>
                <Text style={[styles.nickname, { color: colors.text }]} numberOfLines={1}>
                  {member?.nickname}
                </Text>
                <View style={styles.kakaoTagRow}>
                  <View style={styles.kakaoTag}>
                    <Text style={styles.kakaoTagText}>kakao</Text>
                  </View>
                  <Text style={[styles.accountSub, { color: colors.subText }]}>카카오 계정으로 로그인됨</Text>
                </View>
              </View>
            </View>
            <View style={{ flexDirection: "row", gap: spacing.sm }}>
              <Pressable
                onPress={onLogout}
                style={({ pressed }) => [
                  styles.outlineBtn,
                  { borderColor: colors.border, opacity: pressed ? opacity.pressed : 1, flex: 1 },
                ]}
              >
                <Text style={[styles.outlineLabel, { color: colors.text }]}>로그아웃</Text>
              </Pressable>
              <Pressable
                onPress={onWithdraw}
                style={({ pressed }) => [
                  styles.outlineBtn,
                  { borderColor: colors.border, opacity: pressed ? opacity.pressed : 1 },
                ]}
              >
                <Text style={[styles.outlineLabel, { color: colors.subText }]}>회원 탈퇴</Text>
              </Pressable>
            </View>
          </View>
        ) : (
          <Pressable
            onPress={() => router.push("/login")}
            style={({ pressed }) => [styles.kakaoBtn, { opacity: pressed ? opacity.pressed : 1 }]}
          >
            <Text style={styles.kakaoLabel}>카카오로 로그인</Text>
          </Pressable>
        )}

        <Text style={[styles.sectionTitle, { color: colors.text, marginTop: spacing.xl }]}>응원하는 팀</Text>
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
  accountRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.md,
    padding: spacing.md,
    borderRadius: radius.md,
    borderWidth: border.card,
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: radius.pill,
    backgroundColor: "#FEE500",
    alignItems: "center",
    justifyContent: "center",
  },
  avatarText: { fontSize: 20, fontWeight: "700", color: "#3C1E1E" },
  nickname: { fontSize: 16, fontWeight: "700" },
  kakaoTagRow: { flexDirection: "row", alignItems: "center", gap: 6, marginTop: 4 },
  kakaoTag: {
    backgroundColor: "#FEE500",
    paddingHorizontal: 7,
    paddingVertical: 2,
    borderRadius: 4,
  },
  kakaoTagText: { fontSize: 10, fontWeight: "700", color: "#3C1E1E" },
  accountSub: { fontSize: 12 },
  outlineBtn: {
    paddingVertical: 11,
    paddingHorizontal: spacing.md,
    borderRadius: radius.md,
    borderWidth: border.card,
    alignItems: "center",
  },
  outlineLabel: { fontSize: 14, fontWeight: "600" },
  kakaoBtn: {
    marginTop: spacing.sm,
    backgroundColor: "#FEE500",
    borderRadius: radius.md,
    paddingVertical: 13,
    alignItems: "center",
  },
  kakaoLabel: { fontSize: 15, fontWeight: "700", color: "#3C1E1E" },
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
