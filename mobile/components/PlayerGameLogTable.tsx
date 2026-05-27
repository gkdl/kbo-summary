import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import type { PlayerGameLog } from "../types/player";

interface Props {
  gameLog: PlayerGameLog;
}

export function PlayerGameLogTable({ gameLog }: Props) {
  const { colors } = useTheme();
  const isPitcher = gameLog.playerType === "PITCHER";
  const recordLabel = isPitcher ? "투구기록" : "타격기록";

  if (gameLog.recentGames.length === 0) {
    return (
      <View style={[styles.empty, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Text style={{ color: colors.text, opacity: 0.6 }}>최근 경기 기록이 없습니다</Text>
      </View>
    );
  }

  return (
    <View style={[styles.table, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <View style={[styles.row, styles.header, { borderBottomColor: colors.border }]}>
        <Text style={[styles.cellDate, styles.headerText, { color: colors.text }]}>일자</Text>
        <Text style={[styles.cellOpponent, styles.headerText, { color: colors.text }]}>상대</Text>
        <Text style={[styles.cellRecord, styles.headerText, { color: colors.text }]}>
          {recordLabel}
        </Text>
      </View>
      {gameLog.recentGames.map((entry, index) => (
        <View
          key={`${entry.gameDate ?? "-"}-${index}`}
          style={[
            styles.row,
            {
              borderTopColor: colors.border,
              borderTopWidth: index === 0 ? 0 : StyleSheet.hairlineWidth,
            },
          ]}
        >
          <Text style={[styles.cellDate, { color: colors.text }]} numberOfLines={1}>
            {entry.gameDate ?? "-"}
          </Text>
          <Text style={[styles.cellOpponent, { color: colors.text }]} numberOfLines={1}>
            {entry.opponent ?? "-"}
          </Text>
          <Text style={[styles.cellRecord, { color: colors.text }]} numberOfLines={1}>
            {entry.record}
          </Text>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  table: { borderRadius: 8, borderWidth: 1, overflow: "hidden" },
  row: { flexDirection: "row", paddingHorizontal: 12, paddingVertical: 10, alignItems: "center" },
  header: { borderBottomWidth: StyleSheet.hairlineWidth },
  headerText: { fontWeight: "600", opacity: 0.7 },
  cellDate: { width: 64, fontSize: 13, fontVariant: ["tabular-nums"] },
  cellOpponent: { width: 60, fontSize: 13 },
  cellRecord: { flex: 1, fontSize: 13, textAlign: "right" },
  empty: { padding: 24, alignItems: "center", borderRadius: 8, borderWidth: 1 },
});
