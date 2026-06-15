import { Alert, Platform, Pressable, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useRouter } from "expo-router";

import { useAuth } from "../hooks/useAuth";
import { useTheme } from "../hooks/useTheme";
import { radius, spacing } from "../constants/tokens";

export default function LoginScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { login, loading } = useAuth();

  const onKakao = async () => {
    // 카카오 로그인은 네이티브 모듈이라 웹/Expo Go 에서는 동작하지 않는다
    if (Platform.OS === "web") {
      Alert.alert("안내", "카카오 로그인은 모바일 앱에서만 이용할 수 있어요.");
      return;
    }
    try {
      await login();
      router.back();
    } catch {
      Alert.alert("로그인 실패", "카카오 로그인에 실패했어요. 잠시 후 다시 시도해주세요.");
    }
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.content}>
        <View style={[styles.logo, { backgroundColor: colors.primary }]}>
          <Text style={styles.logoText}>⚾</Text>
        </View>
        <Text style={[styles.title, { color: colors.text }]}>KBO 경기요약</Text>
        <Text style={[styles.subtitle, { color: colors.subText }]}>
          경기 결과부터 팬들의 이야기까지{"\n"}한 곳에서
        </Text>

        <Pressable
          onPress={onKakao}
          disabled={loading}
          style={({ pressed }) => [styles.kakaoButton, { opacity: pressed || loading ? 0.7 : 1 }]}
        >
          <Text style={styles.kakaoText}>{loading ? "로그인 중..." : "카카오로 시작하기"}</Text>
        </Pressable>

        <Pressable onPress={() => router.back()} style={styles.skip}>
          <Text style={[styles.skipText, { color: colors.subText }]}>로그인 없이 둘러보기 ›</Text>
        </Pressable>
      </View>

      <Text style={[styles.terms, { color: colors.subText }]}>
        로그인 시 이용약관 및 개인정보처리방침에 동의하게 됩니다
      </Text>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: "center", paddingHorizontal: spacing.xl },
  content: { alignItems: "center" },
  logo: {
    width: 76,
    height: 76,
    borderRadius: radius.lg,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: spacing.lg,
  },
  logoText: { fontSize: 38 },
  title: { fontSize: 22, fontWeight: "700", marginBottom: spacing.sm },
  subtitle: { fontSize: 14, textAlign: "center", lineHeight: 22, marginBottom: spacing.xl + 12 },
  kakaoButton: {
    width: "100%",
    backgroundColor: "#FEE500",
    borderRadius: radius.md,
    paddingVertical: 14,
    alignItems: "center",
  },
  kakaoText: { fontSize: 15, fontWeight: "700", color: "#3C1E1E" },
  skip: { marginTop: spacing.lg },
  skipText: { fontSize: 14 },
  terms: {
    position: "absolute",
    bottom: spacing.xl,
    left: spacing.xl,
    right: spacing.xl,
    fontSize: 11,
    textAlign: "center",
    lineHeight: 16,
  },
});
