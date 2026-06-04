import { StyleSheet, View } from "react-native";

import { useTheme } from "../../hooks/useTheme";
import { border, radius, spacing } from "../../constants/tokens";
import { SkeletonLoader } from "../SkeletonLoader";

export function HighlightCardSkeleton() {
  const { colors } = useTheme();

  return (
    <View
      style={[
        styles.card,
        { backgroundColor: colors.card, borderColor: colors.border },
      ]}
    >
      <View style={[styles.thumb, { backgroundColor: colors.border }]} />
      <View style={styles.meta}>
        <View style={styles.row}>
          <SkeletonLoader width={70} height={14} borderRadius={3} />
          <SkeletonLoader width={50} height={14} borderRadius={3} />
          <SkeletonLoader width={70} height={14} borderRadius={3} />
        </View>
        <SkeletonLoader width={220} height={12} borderRadius={3} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { borderRadius: radius.md, borderWidth: border.card, overflow: "hidden" },
  thumb: { width: "100%", aspectRatio: 16 / 9 },
  meta: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm + 2, gap: spacing.sm },
  row: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
});
