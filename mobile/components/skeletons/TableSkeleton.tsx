import { StyleSheet, View } from "react-native";

import { useTheme } from "../../hooks/useTheme";
import { border, radius, spacing } from "../../constants/tokens";
import { SkeletonLoader } from "../SkeletonLoader";

interface Props {
  rows?: number;
}

// 순위표·박스스코어 등 표 형태 로딩에 쓰는 범용 스켈레톤
export function TableSkeleton({ rows = 6 }: Props) {
  const { colors } = useTheme();
  return (
    <View style={[styles.wrap, { backgroundColor: colors.card, borderColor: colors.border }]}>
      {Array.from({ length: rows }).map((_, i) => (
        <View key={i} style={styles.row}>
          <SkeletonLoader width={24} height={14} borderRadius={3} />
          <SkeletonLoader width={120} height={14} borderRadius={3} />
          <SkeletonLoader width={40} height={14} borderRadius={3} />
          <SkeletonLoader width={40} height={14} borderRadius={3} />
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    borderRadius: radius.md,
    borderWidth: border.card,
    padding: spacing.md,
    gap: spacing.md,
  },
  row: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.sm,
  },
});
