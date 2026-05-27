import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";
import type { Game } from "../types/game";

interface Props {
  game: Game;
}

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: "예정",
  IN_PROGRESS: "경기중",
  FINISHED: "종료",
};

export function ScoreCard({ game }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const away = getTeam(game.awayTeamCode);
  const home = getTeam(game.homeTeamCode);
  // 점수는 항상 표시 — 예정/진행 중 경기는 KBO 가 null 또는 0 을 주므로 ?? 0 으로 0:0 fallback.
  const awayScore = game.awayScore ?? 0;
  const homeScore = game.homeScore ?? 0;

  return (
    <Pressable
      onPress={() => router.push(`/game/${game.gameId}`)}
      style={({ pressed }) => [
        styles.card,
        { backgroundColor: colors.card, borderColor: colors.border, opacity: pressed ? 0.7 : 1 },
      ]}
    >
      <Text style={[styles.meta, { color: colors.text, opacity: 0.6 }]}>
        {game.gameDate}
        {game.startTime ? ` · ${game.startTime}` : ""}
        {game.stadium ? ` · ${game.stadium}` : ""}
      </Text>

      <View style={styles.row}>
        <View style={styles.teamSide}>
          <View style={[styles.badge, { backgroundColor: away?.color ?? colors.primary }]} />
          <Text style={[styles.team, { color: colors.text }]}>
            {away?.shortName ?? game.awayTeamCode}
          </Text>
        </View>
        <Text style={[styles.score, { color: colors.text }]}>
          {awayScore} : {homeScore}
        </Text>
        <View style={[styles.teamSide, styles.teamSideRight]}>
          <Text style={[styles.team, { color: colors.text }]}>
            {home?.shortName ?? game.homeTeamCode}
          </Text>
          <View style={[styles.badge, { backgroundColor: home?.color ?? colors.primary }]} />
        </View>
      </View>

      <Text style={[styles.status, { color: colors.text, opacity: 0.5 }]}>
        {STATUS_LABEL[game.status] ?? game.status}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { padding: 14, borderRadius: 8, borderWidth: 1, gap: 8 },
  meta: { fontSize: 12 },
  row: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: 12 },
  teamSide: { flexDirection: "row", alignItems: "center", gap: 8, flex: 1 },
  teamSideRight: { justifyContent: "flex-end" },
  badge: { width: 10, height: 10, borderRadius: 5 },
  team: { fontSize: 16, fontWeight: "600" },
  score: { fontSize: 22, fontWeight: "700", fontVariant: ["tabular-nums"] },
  status: { fontSize: 11 },
});
