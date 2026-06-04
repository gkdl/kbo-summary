import { StyleSheet, Text, View } from "react-native";

import { useTheme } from "../hooks/useTheme";
import { getTeam } from "../constants/teams";
import { border, radius, spacing } from "../constants/tokens";
import type { Game } from "../types/game";

interface Props {
  game: Game;
}

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: "경기 예정",
  IN_PROGRESS: "경기 중",
  FINISHED: "경기 종료",
};

const STATUS_COLOR: Record<string, string> = {
  IN_PROGRESS: "#34C759",
  FINISHED: "#8E8E93",
};

export function GameScoreHero({ game }: Props) {
  const { colors } = useTheme();
  const away = getTeam(game.awayTeamCode);
  const home = getTeam(game.homeTeamCode);

  const scheduled = game.status === "SCHEDULED";
  const live = game.status === "IN_PROGRESS";
  const finished = game.status === "FINISHED";

  const hasScore = !scheduled && (game.awayScore != null || game.homeScore != null);
  const awayScore = game.awayScore ?? 0;
  const homeScore = game.homeScore ?? 0;
  const awayWins = hasScore && awayScore > homeScore;
  const homeWins = hasScore && homeScore > awayScore;

  // 종료 경기에서만 패자 dim — 진행 중엔 역전 여지가 있어 둘 다 풀 컬러
  const awayScoreColor = finished && homeWins ? colors.subText : colors.text;
  const homeScoreColor = finished && awayWins ? colors.subText : colors.text;
  const awayTeamColor = finished && homeWins ? colors.subText : colors.text;
  const homeTeamColor = finished && awayWins ? colors.subText : colors.text;

  const statusColor = STATUS_COLOR[game.status] ?? colors.subText;

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      {/* 팀 컬러 좌우 스트라이프 */}
      <View style={[styles.stripe, styles.stripeLeft, { backgroundColor: away?.color ?? colors.primary }]} />
      <View style={[styles.stripe, styles.stripeRight, { backgroundColor: home?.color ?? colors.primary }]} />

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
          <Text style={[styles.teamName, { color: awayTeamColor }]} numberOfLines={1}>
            {away?.name ?? game.awayTeamCode}
          </Text>
        </View>

        <View style={styles.scoreBlock}>
          {hasScore ? (
            <Text style={styles.score}>
              <Text style={{ color: awayScoreColor, fontWeight: awayWins ? "800" : "700" }}>
                {awayScore}
              </Text>
              <Text style={[styles.colon, { color: colors.subText }]}>{"  "}:{"  "}</Text>
              <Text style={{ color: homeScoreColor, fontWeight: homeWins ? "800" : "700" }}>
                {homeScore}
              </Text>
            </Text>
          ) : (
            <Text style={[styles.vs, { color: colors.subText }]}>vs</Text>
          )}
        </View>

        <View style={[styles.teamSide]}>
          <View style={[styles.bigBadge, { backgroundColor: home?.color ?? colors.primary }]}>
            <Text style={styles.bigBadgeText}>{home?.shortName ?? game.homeTeamCode}</Text>
          </View>
          <Text style={[styles.teamName, { color: homeTeamColor }]} numberOfLines={1}>
            {home?.name ?? game.homeTeamCode}
          </Text>
        </View>
      </View>

      <View style={styles.statusRow}>
        <View style={[styles.statusChip, { backgroundColor: statusColor }]}>
          {live && <View style={styles.liveDot} />}
          <Text style={styles.statusText}>{STATUS_LABEL[game.status] ?? game.status}</Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    padding: spacing.lg,
    borderRadius: radius.md,
    borderWidth: border.card,
    gap: spacing.md,
    overflow: "hidden",
  },
  stripe: {
    position: "absolute",
    top: 0,
    bottom: 0,
    width: 4,
  },
  stripeLeft: { left: 0 },
  stripeRight: { right: 0 },
  meta: { fontSize: 12, textAlign: "center" },
  row: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.sm,
  },
  teamSide: { alignItems: "center", flex: 1, gap: 6 },
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
  teamName: { fontSize: 12, fontWeight: "600" },
  scoreBlock: { minWidth: 100, alignItems: "center" },
  score: { fontSize: 44, fontWeight: "700", fontVariant: ["tabular-nums"], textAlign: "center" },
  colon: { fontSize: 32, fontWeight: "400" },
  vs: { fontSize: 26, fontWeight: "700", letterSpacing: 2 },
  statusRow: { alignItems: "center" },
  statusChip: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    paddingHorizontal: spacing.md,
    paddingVertical: 4,
    borderRadius: radius.pill,
  },
  liveDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: "#FFFFFF",
  },
  statusText: { color: "#FFFFFF", fontSize: 12, fontWeight: "700" },
});
