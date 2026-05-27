import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";
import type { TeamDetail } from "../types/team";
import { RecentFormDots } from "./RecentFormDots";

interface Props {
  team: TeamDetail;
  recentForm?: string[];
}

export function TeamCard({ team, recentForm }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const meta = getTeam(team.teamCode);
  const accent = meta?.color ?? colors.primary;

  return (
    <Pressable
      onPress={() => router.push(`/team/${team.teamCode}`)}
      style={({ pressed }) => [
        styles.card,
        { backgroundColor: colors.card, borderColor: colors.border, opacity: pressed ? 0.7 : 1 },
      ]}
    >
      <View style={[styles.accent, { backgroundColor: accent }]} />
      <View style={styles.content}>
        <View style={styles.headerRow}>
          {team.rank !== null ? (
            <Text style={[styles.rank, { color: colors.text }]}>{team.rank}위</Text>
          ) : null}
          <Text style={[styles.name, { color: colors.text }]}>{team.teamName}</Text>
        </View>
        <Text style={[styles.record, { color: colors.text, opacity: 0.7 }]}>
          {team.wins}승 {team.losses}패 {team.draws}무
        </Text>
        {recentForm && recentForm.length > 0 ? (
          <RecentFormDots recentForm={recentForm} />
        ) : null}
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { flexDirection: "row", borderRadius: 8, borderWidth: 1, overflow: "hidden" },
  accent: { width: 4 },
  content: { flex: 1, padding: 12, gap: 4 },
  headerRow: { flexDirection: "row", alignItems: "baseline", gap: 8 },
  rank: { fontSize: 18, fontWeight: "700" },
  name: { fontSize: 16, fontWeight: "600" },
  record: { fontSize: 13 },
});
