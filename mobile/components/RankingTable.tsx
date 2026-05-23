import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";
import { useTheme } from "@react-navigation/native";

import { getTeam } from "../constants/teams";
import { useAppStore } from "../store/useAppStore";
import type { Standing } from "../types/team";

interface Props {
  standings: Standing[];
}

export function RankingTable({ standings }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const myTeam = useAppStore((state) => state.myTeam);

  if (standings.length === 0) {
    return (
      <View style={[styles.empty, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Text style={{ color: colors.text, opacity: 0.6 }}>순위 데이터가 없습니다</Text>
      </View>
    );
  }

  return (
    <View style={[styles.table, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <View style={[styles.row, styles.header, { borderBottomColor: colors.border }]}>
        <Text style={[styles.headerText, styles.cellRank, { color: colors.text }]}>순위</Text>
        <Text style={[styles.headerText, styles.cellTeam, { color: colors.text }]}>팀</Text>
        <Text style={[styles.headerText, styles.cellNum, { color: colors.text }]}>승</Text>
        <Text style={[styles.headerText, styles.cellNum, { color: colors.text }]}>패</Text>
        <Text style={[styles.headerText, styles.cellNum, { color: colors.text }]}>무</Text>
        <Text style={[styles.headerText, styles.cellRate, { color: colors.text }]}>승률</Text>
        <Text style={[styles.headerText, styles.cellGb, { color: colors.text }]}>GB</Text>
      </View>
      {standings.map((row) => {
        const team = getTeam(row.teamCode);
        const isMyTeam = myTeam !== null && row.teamCode === myTeam;
        return (
          <Pressable
            key={row.teamCode}
            onPress={() => router.push(`/teams/${row.teamCode}`)}
            style={({ pressed }) => [
              styles.row,
              {
                borderTopColor: colors.border,
                borderTopWidth: StyleSheet.hairlineWidth,
                backgroundColor: isMyTeam ? (team?.color ?? colors.primary) + "20" : "transparent",
                opacity: pressed ? 0.6 : 1,
              },
            ]}
          >
            <Text
              style={[
                styles.cellRank,
                styles.cellText,
                { color: colors.text, fontWeight: isMyTeam ? "700" : "600" },
              ]}
            >
              {row.rank}
            </Text>
            <View style={styles.cellTeam}>
              <View style={[styles.dot, { backgroundColor: team?.color ?? colors.primary }]} />
              <Text
                style={[
                  styles.teamName,
                  { color: colors.text, fontWeight: isMyTeam ? "700" : "500" },
                ]}
                numberOfLines={1}
              >
                {team?.shortName ?? row.teamCode}
              </Text>
            </View>
            <Text style={[styles.cellNum, styles.cellText, { color: colors.text }]}>
              {row.wins}
            </Text>
            <Text style={[styles.cellNum, styles.cellText, { color: colors.text }]}>
              {row.losses}
            </Text>
            <Text style={[styles.cellNum, styles.cellText, { color: colors.text }]}>
              {row.draws}
            </Text>
            <Text style={[styles.cellRate, styles.cellText, { color: colors.text }]}>
              {row.winRate?.toFixed(3) ?? "-"}
            </Text>
            <Text style={[styles.cellGb, styles.cellText, { color: colors.text }]}>
              {row.gamesBehind?.toFixed(1) ?? "-"}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  table: { borderRadius: 8, borderWidth: 1, overflow: "hidden" },
  row: { flexDirection: "row", paddingHorizontal: 8, paddingVertical: 10, alignItems: "center" },
  header: { borderBottomWidth: StyleSheet.hairlineWidth },
  headerText: { fontSize: 12, fontWeight: "600", opacity: 0.7 },
  cellText: { fontSize: 13, fontVariant: ["tabular-nums"] },
  cellRank: { width: 36, textAlign: "center" },
  cellTeam: { flex: 1, flexDirection: "row", alignItems: "center", gap: 6 },
  cellNum: { width: 30, textAlign: "center" },
  cellRate: { width: 50, textAlign: "center" },
  cellGb: { width: 40, textAlign: "center" },
  dot: { width: 8, height: 8, borderRadius: 4 },
  teamName: { fontSize: 14 },
  empty: { padding: 24, alignItems: "center", borderRadius: 8, borderWidth: 1 },
});
