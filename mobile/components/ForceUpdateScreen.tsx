import { Alert, Linking, Pressable, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { useTheme } from "../hooks/useTheme";
import { border, opacity, radius, spacing } from "../constants/tokens";

interface Props {
  storeUrl: string;
  currentVersion: number | null;
  minVersion: number | null;
}

/**
 * 강제 업데이트 풀스크린 — 닫기/취소 없음. 사용자가 Play Store 로 이동해서 업데이트해야만 빠져나갈 수 있다.
 *
 * 의도적으로 swipe/back 으로 dismiss 불가능 — _layout.tsx 에서 라우터 마운트 전에
 * 이 컴포넌트로 분기하기 때문에 네비게이션 자체가 차단된다.
 */
export function ForceUpdateScreen({ storeUrl, currentVersion, minVersion }: Props) {
  const { colors } = useTheme();

  const openStore = async () => {
    try {
      await Linking.openURL(storeUrl);
    } catch {
      Alert.alert("스토어를 열 수 없습니다", storeUrl);
    }
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.content}>
        <Text style={[styles.icon, { color: colors.primary }]}>⬆</Text>
        <Text style={[styles.title, { color: colors.text }]}>업데이트가 필요합니다</Text>
        <Text style={[styles.body, { color: colors.subText }]}>
          앱을 계속 사용하려면 최신 버전으로 업데이트해 주세요.
        </Text>

        {currentVersion != null && minVersion != null ? (
          <View style={[styles.versionBox, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <View style={styles.versionRow}>
              <Text style={[styles.versionLabel, { color: colors.subText }]}>현재 버전</Text>
              <Text style={[styles.versionValue, { color: colors.text }]}>build {currentVersion}</Text>
            </View>
            <View style={styles.versionRow}>
              <Text style={[styles.versionLabel, { color: colors.subText }]}>필요 버전</Text>
              <Text style={[styles.versionValue, { color: colors.text }]}>build {minVersion} 이상</Text>
            </View>
          </View>
        ) : null}

        <Pressable
          onPress={openStore}
          accessibilityRole="button"
          accessibilityLabel="스토어에서 업데이트하기"
          style={({ pressed }) => [
            styles.cta,
            { backgroundColor: colors.primary },
            pressed && { opacity: opacity.pressed },
          ]}
        >
          <Text style={styles.ctaText}>스토어에서 업데이트하기</Text>
        </Pressable>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: spacing.xl,
    gap: spacing.md,
  },
  icon: { fontSize: 56, fontWeight: "700", marginBottom: spacing.sm },
  title: { fontSize: 22, fontWeight: "800", textAlign: "center" },
  body: { fontSize: 14, textAlign: "center", lineHeight: 22 },
  versionBox: {
    width: "100%",
    marginTop: spacing.lg,
    padding: spacing.md,
    borderRadius: radius.md,
    borderWidth: border.card,
    gap: spacing.sm,
  },
  versionRow: { flexDirection: "row", justifyContent: "space-between" },
  versionLabel: { fontSize: 13 },
  versionValue: { fontSize: 13, fontWeight: "700" },
  cta: {
    marginTop: spacing.xl,
    paddingVertical: 14,
    paddingHorizontal: spacing.xl,
    borderRadius: radius.md,
    width: "100%",
    alignItems: "center",
  },
  ctaText: { color: "#FFFFFF", fontSize: 16, fontWeight: "700" },
});
