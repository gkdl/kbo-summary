import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import type { GameSummary } from "../types/game";
import { SkeletonLoader } from "./SkeletonLoader";

interface Props {
  summary: GameSummary | null;
  loading?: boolean;
}

export function AISummaryCard({ summary, loading }: Props) {
  const { colors } = useTheme();

  return (
    <View
      style={[
        styles.card,
        { backgroundColor: colors.card, borderColor: colors.primary },
      ]}
    >
      <View style={[styles.accent, { backgroundColor: colors.primary }]} />
      <View style={styles.header}>
        <Text style={[styles.title, { color: colors.primary }]}>✦ AI 경기 요약</Text>
      </View>

      {loading ? (
        <View style={{ gap: 6 }}>
          <SkeletonLoader height={14} />
          <SkeletonLoader height={14} />
          <SkeletonLoader height={14} />
        </View>
      ) : summary ? (
        <>
          <Text style={[styles.body, { color: colors.text }]}>{summary.summary}</Text>
          <Text style={[styles.timestamp, { color: colors.subText }]}>
            생성: {summary.createdAt.replace("T", " ").slice(0, 16)}
          </Text>
        </>
      ) : (
        <Text style={[styles.body, { color: colors.subText }]}>
          요약을 불러올 수 없습니다
        </Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: { paddingHorizontal: 14, paddingBottom: 14, paddingTop: 10, borderRadius: 10, borderWidth: 1, gap: 8, overflow: "hidden" },
  accent: { position: "absolute", top: 0, left: 0, right: 0, height: 3 },
  header: { flexDirection: "row", alignItems: "center", gap: 6, marginTop: 4 },
  title: { fontSize: 14, fontWeight: "700" },
  body: { fontSize: 14, lineHeight: 22 },
  timestamp: { fontSize: 11 },
});
