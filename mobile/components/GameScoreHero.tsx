import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";
import type { Game } from "../types/game";

interface Props {
  game: Game;
}

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: "경기 예정",
  IN_PROGRESS: "경기 중",
  FINISHED: "경기 종료",
};

export function GameScoreHero({ game }: Props) {
  const { colors } = useTheme();
  const away = getTeam(game.awayTeamCode);
  const home = getTeam(game.homeTeamCode);
  // 모든 상태에서 점수 표시. 예정/진행 중에 점수가 없으면 0:0 으로 fallback.
  const awayScore = game.awayScore ?? 0;
  const homeScore = game.homeScore ?? 0;

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <Text style={[styles.meta, { color: colors.subText }]}>
        {game.gameDate}
        {game.startTime ? ` · ${game.startTime}` : ""}
        {game.stadium ? ` · ${game.stadium}` : ""}
      </Text>

      <View style={styles.row}>
        <View style={styles.teamSide}>
          <View style={[styles.bigBadge, { backgroundColor: away?.color ?? colors.primary }]}>
            <Text style={styles.bigBadgeText}>{away?.shortName ?? game.awayTeamCode}</Text>
          </View>
          <Text style={[styles.teamName, { color: colors.text }]} numberOfLines={1}>
            {away?.name ?? game.awayTeamCode}
          </Text>
        </View>

        <View style={styles.scoreBlock}>
          <Text style={[styles.score, { color: colors.text }]}>
            <Text>{awayScore}</Text>
            <Text style={[styles.colon, { color: colors.subText }]}> : </Text>
            <Text>{homeScore}</Text>
          </Text>
        </View>

        <View style={[styles.teamSide, styles.teamSideRight]}>
          <View style={[styles.bigBadge, { backgroundColor: home?.color ?? colors.primary }]}>
            <Text style={styles.bigBadgeText}>{home?.shortName ?? game.homeTeamCode}</Text>
          </View>
          <Text style={[styles.teamName, { color: colors.text }]} numberOfLines={1}>
            {home?.name ?? game.homeTeamCode}
          </Text>
        </View>
      </View>

      <Text style={[styles.status, { color: colors.subText }]}>
        {STATUS_LABEL[game.status] ?? game.status}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 16, borderRadius: 10, borderWidth: 1, gap: 12 },
  meta: { fontSize: 12, textAlign: "center" },
  row: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: 8 },
  teamSide: { alignItems: "center", flex: 1, gap: 6 },
  teamSideRight: {},
  bigBadge: {
    minWidth: 60,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 6,
    alignItems: "center",
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.2)",
  },
  bigBadgeText: { color: "#FFFFFF", fontSize: 14, fontWeight: "700" },
  teamName: { fontSize: 12, opacity: 0.8 },
  scoreBlock: { minWidth: 80, alignItems: "center" },
  score: { fontSize: 36, fontWeight: "800", fontVariant: ["tabular-nums"] },
  colon: { fontSize: 28, fontWeight: "400" },
  vs: { fontSize: 22, fontWeight: "700", letterSpacing: 2 },
  status: { fontSize: 12, textAlign: "center" },
});
