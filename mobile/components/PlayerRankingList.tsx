import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";
import type { PlayerRanking } from "../types/player";

interface Props {
  rankings: PlayerRanking[];
}

export function PlayerRankingList({ rankings }: Props) {
  const { colors } = useTheme();
  const router = useRouter();

  if (rankings.length === 0) {
    return (
      <View style={[styles.empty, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Text style={{ color: colors.subText }}>순위 데이터가 없습니다</Text>
      </View>
    );
  }

  return (
    <View style={[styles.list, { backgroundColor: colors.card, borderColor: colors.border }]}>
      {rankings.map((row, index) => {
        const team = row.teamCode ? getTeam(row.teamCode) : undefined;
        return (
          <Pressable
            key={row.playerId}
            onPress={() => router.push(`/player/${row.playerId}`)}
            style={({ pressed }) => [
              styles.row,
              {
                borderTopColor: colors.border,
                borderTopWidth: index === 0 ? 0 : StyleSheet.hairlineWidth,
                opacity: pressed ? 0.6 : 1,
              },
            ]}
          >
            <Text style={[styles.rank, { color: colors.text }]}>{row.rank}</Text>
            <View style={[styles.teamDot, { backgroundColor: team?.color ?? colors.primary }]} />
            <View style={styles.nameCol}>
              <Text style={[styles.name, { color: colors.text }]} numberOfLines={1}>
                {row.playerName}
              </Text>
              <Text style={[styles.team, { color: colors.subText }]}>
                {team?.shortName ?? row.teamCode ?? ""}
              </Text>
            </View>
            <Text style={[styles.value, { color: colors.text }]}>{row.value}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  list: { borderRadius: 8, borderWidth: 1, overflow: "hidden" },
  row: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 12,
    paddingVertical: 10,
    gap: 10,
  },
  rank: { width: 28, fontSize: 15, fontWeight: "700", textAlign: "center" },
  teamDot: { width: 8, height: 8, borderRadius: 4 },
  nameCol: { flex: 1 },
  name: { fontSize: 15, fontWeight: "500" },
  team: { fontSize: 11, marginTop: 2 },
  value: { fontSize: 15, fontWeight: "600", fontVariant: ["tabular-nums"] },
  empty: { padding: 24, alignItems: "center", borderRadius: 8, borderWidth: 1 },
});
