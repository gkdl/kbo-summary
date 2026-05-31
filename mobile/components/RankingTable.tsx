import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";
import { useAppStore } from "../store/useAppStore";
import type { Standing } from "../types/team";
import { RecentFormDots } from "./RecentFormDots";

interface Props {
  standings: Standing[];
  recentForms?: Record<string, string[]>;
}

export function RankingTable({ standings, recentForms }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const myTeam = useAppStore((state) => state.myTeam);
  const showRecent = recentForms !== undefined;

  if (standings.length === 0) {
    return (
      <View style={[styles.empty, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Text style={{ color: colors.subText }}>순위 데이터가 없습니다</Text>
      </View>
    );
  }

  return (
    <View style={[styles.table, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <View style={[styles.row, styles.header, { borderBottomColor: colors.border }]}>
        <Text style={[styles.headerText, styles.cellRank, { color: colors.subText }]}>순위</Text>
        <Text style={[styles.headerText, styles.cellTeam, { color: colors.subText }]}>팀</Text>
        <Text style={[styles.headerText, styles.cellNum, { color: colors.subText }]}>승</Text>
        <Text style={[styles.headerText, styles.cellNum, { color: colors.subText }]}>패</Text>
        <Text style={[styles.headerText, styles.cellNum, { color: colors.subText }]}>무</Text>
        <Text style={[styles.headerText, styles.cellRate, { color: colors.subText }]}>승률</Text>
        <Text style={[styles.headerText, styles.cellGb, { color: colors.subText }]}>GB</Text>
        {showRecent ? (
          <Text style={[styles.headerText, styles.cellRecent, { color: colors.subText }]}>최근 5</Text>
        ) : null}
      </View>
      {standings.map((row) => {
        const team = getTeam(row.teamCode);
        const isMyTeam = myTeam !== null && row.teamCode === myTeam;
        const recent = recentForms?.[row.teamCode];
        return (
          <Pressable
            key={row.teamCode}
            onPress={() => router.push(`/team/${row.teamCode}`)}
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
            {showRecent ? (
              <View style={styles.cellRecent}>
                {recent && recent.length > 0 ? (
                  <RecentFormDots recentForm={recent.slice(0, 5)} size={10} />
                ) : null}
              </View>
            ) : null}
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
  headerText: { fontSize: 12, fontWeight: "600" },
  cellText: { fontSize: 13, fontVariant: ["tabular-nums"] },
  cellRank: { width: 32, textAlign: "center" },
  cellTeam: { flex: 1, flexDirection: "row", alignItems: "center", gap: 6 },
  cellNum: { width: 26, textAlign: "center" },
  cellRate: { width: 46, textAlign: "center" },
  cellGb: { width: 36, textAlign: "center" },
  cellRecent: { width: 64, alignItems: "center" },
  dot: { width: 8, height: 8, borderRadius: 4, borderWidth: 1, borderColor: "rgba(255,255,255,0.2)" },
  teamName: { fontSize: 14 },
  empty: { padding: 24, alignItems: "center", borderRadius: 8, borderWidth: 1 },
});
