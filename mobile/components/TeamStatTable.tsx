import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import type { TeamStats } from "../types/team";

interface Props {
  stats: TeamStats;
}

export function TeamStatTable({ stats }: Props) {
  const { colors } = useTheme();

  const rows: [string, string][] = [
    ["순위", `${stats.rank}위`],
    ["승", stats.wins.toString()],
    ["패", stats.losses.toString()],
    ["무", stats.draws.toString()],
    ["승률", stats.winRate?.toFixed(3) ?? "-"],
    ["게임차", stats.gamesBehind?.toFixed(1) ?? "-"],
  ];

  return (
    <View style={[styles.table, { borderColor: colors.border, backgroundColor: colors.card }]}>
      {rows.map(([label, value], index) => (
        <View
          key={label}
          style={[
            styles.row,
            {
              borderTopColor: colors.border,
              borderTopWidth: index === 0 ? 0 : StyleSheet.hairlineWidth,
            },
          ]}
        >
          <Text style={[styles.label, { color: colors.subText }]}>{label}</Text>
          <Text style={[styles.value, { color: colors.text }]}>{value}</Text>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  table: { borderRadius: 8, borderWidth: 1, overflow: "hidden" },
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  label: { fontSize: 14 },
  value: { fontSize: 15, fontWeight: "600", fontVariant: ["tabular-nums"] },
});
