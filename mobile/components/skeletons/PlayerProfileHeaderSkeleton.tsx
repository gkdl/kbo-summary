import { StyleSheet, View } from "react-native";

import { useTheme } from "../../hooks/useTheme";
import { border, radius, spacing } from "../../constants/tokens";
import { SkeletonLoader } from "../SkeletonLoader";

export function PlayerProfileHeaderSkeleton() {
  const { colors } = useTheme();

  return (
    <View
      style={[
        styles.card,
        { backgroundColor: colors.card, borderColor: colors.border },
      ]}
    >
      <SkeletonLoader width={56} height={56} borderRadius={radius.pill} />
      <View style={styles.textCol}>
        <SkeletonLoader width={120} height={22} borderRadius={4} />
        <SkeletonLoader width={180} height={14} borderRadius={3} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.md + 2,
    padding: spacing.lg,
    borderRadius: radius.md,
    borderWidth: border.card,
  },
  textCol: { gap: 6 },
});
