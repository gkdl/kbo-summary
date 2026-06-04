import { StyleSheet, View } from "react-native";

import { useTheme } from "../../hooks/useTheme";
import { border, radius, spacing } from "../../constants/tokens";
import { SkeletonLoader } from "../SkeletonLoader";

interface Props {
  variant?: "default" | "hero";
}

// 실제 ScoreCard 와 같은 패딩·radius·줄간격을 사용해 로드 직후 레이아웃 점프가 없도록 한다
export function ScoreCardSkeleton({ variant = "default" }: Props) {
  const { colors } = useTheme();
  const isHero = variant === "hero";

  return (
    <View
      style={[
        styles.card,
        {
          backgroundColor: colors.card,
          borderColor: colors.border,
          paddingVertical: isHero ? spacing.lg : spacing.md,
        },
      ]}
    >
      <View style={styles.row}>
        <SkeletonLoader width={48} height={isHero ? 22 : 18} borderRadius={4} />
        <SkeletonLoader width={isHero ? 120 : 100} height={isHero ? 36 : 30} borderRadius={6} />
        <SkeletonLoader width={48} height={isHero ? 22 : 18} borderRadius={4} />
      </View>
      <View style={styles.metaRow}>
        <SkeletonLoader width={140} height={12} borderRadius={3} />
        <SkeletonLoader width={42} height={18} borderRadius={radius.pill} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: radius.md,
    borderWidth: border.card,
    paddingHorizontal: spacing.md,
    gap: spacing.sm,
  },
  row: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.sm,
  },
  metaRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
});
