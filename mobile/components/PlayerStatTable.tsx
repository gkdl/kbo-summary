import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import type { HittingLine, PitchingLine, PlayerStat } from "../types/player";

interface Props {
  stat: PlayerStat;
}

export function PlayerStatTable({ stat }: Props) {
  const { colors } = useTheme();

  if (stat.hitting) {
    return <StatGrid colors={colors} rows={hittingRows(stat.hitting)} />;
  }
  if (stat.pitching) {
    return <StatGrid colors={colors} rows={pitchingRows(stat.pitching)} />;
  }
  return (
    <View style={[styles.empty, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <Text style={{ color: colors.subText }}>시즌 기록이 없습니다</Text>
    </View>
  );
}

function hittingRows(line: HittingLine): [string, string][] {
  return [
    ["타율", line.avg?.toFixed(3) ?? "-"],
    ["출루+장타(OPS)", line.ops?.toFixed(3) ?? "-"],
    ["경기", line.games.toString()],
    ["타수", line.atBats.toString()],
    ["안타", line.hits.toString()],
    ["홈런", line.homeRuns.toString()],
    ["타점", line.rbi.toString()],
    ["득점", line.runs.toString()],
    ["도루", line.stolenBases.toString()],
    ["볼넷", line.walks.toString()],
    ["삼진", line.strikeOuts.toString()],
  ];
}

function pitchingRows(line: PitchingLine): [string, string][] {
  return [
    ["평균자책(ERA)", line.era?.toFixed(2) ?? "-"],
    ["WHIP", line.whip?.toFixed(2) ?? "-"],
    ["경기", line.games.toString()],
    ["승", line.wins.toString()],
    ["패", line.losses.toString()],
    ["세이브", line.saves.toString()],
    ["홀드", line.holds.toString()],
    ["이닝", line.inningsPitched?.toFixed(1) ?? "-"],
    ["피안타", line.hits.toString()],
    ["볼넷", line.walks.toString()],
    ["탈삼진", line.strikeOuts.toString()],
  ];
}

interface GridProps {
  colors: { text: string; border: string; card: string };
  rows: [string, string][];
}

function StatGrid({ colors, rows }: GridProps) {
  return (
    <View style={[styles.table, { backgroundColor: colors.card, borderColor: colors.border }]}>
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
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  label: { fontSize: 14 },
  value: { fontSize: 14, fontWeight: "600", fontVariant: ["tabular-nums"] },
  empty: { padding: 24, alignItems: "center", borderRadius: 8, borderWidth: 1 },
});
