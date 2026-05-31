import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";
import type { HeadToHead } from "../types/team";

interface Props {
  data: HeadToHead;
}

export function HeadToHeadCard({ data }: Props) {
  const { colors } = useTheme();
  const a = getTeam(data.teamA);
  const b = getTeam(data.teamB);

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <Text style={[styles.title, { color: colors.subText }]}>시즌 상대전적</Text>
      <View style={styles.row}>
        <View style={styles.side}>
          <View style={[styles.badge, { backgroundColor: a?.color ?? colors.primary }]} />
          <Text style={[styles.team, { color: colors.text }]}>
            {a?.shortName ?? data.teamA}
          </Text>
        </View>
        <Text style={[styles.score, { color: colors.text }]}>
          {data.teamAWins}
          <Text style={[styles.divider, { color: colors.subText }]}> : </Text>
          {data.teamBWins}
        </Text>
        <View style={[styles.side, styles.sideRight]}>
          <Text style={[styles.team, { color: colors.text }]}>
            {b?.shortName ?? data.teamB}
          </Text>
          <View style={[styles.badge, { backgroundColor: b?.color ?? colors.primary }]} />
        </View>
      </View>
      <Text style={[styles.footer, { color: colors.subText }]}>
        무승부 {data.draws} · 총 {data.games.length}경기
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 14, borderRadius: 8, borderWidth: 1, gap: 8 },
  title: { fontSize: 12 },
  row: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: 12 },
  side: { flexDirection: "row", alignItems: "center", gap: 8, flex: 1 },
  sideRight: { justifyContent: "flex-end" },
  badge: { width: 10, height: 10, borderRadius: 5 },
  team: { fontSize: 16, fontWeight: "600" },
  score: { fontSize: 24, fontWeight: "700", fontVariant: ["tabular-nums"] },
  divider: { fontSize: 18, fontWeight: "400" },
  footer: { fontSize: 12, textAlign: "center" },
});
