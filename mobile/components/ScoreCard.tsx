import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";

import { useTheme } from "../hooks/useTheme";
import { getTeam } from "../constants/teams";
import { border, opacity, radius, spacing } from "../constants/tokens";
import type { Game } from "../types/game";

interface Props {
  game: Game;
  // hero: MY TEAM 자리처럼 더 크게 강조. default: 일반 리스트 카드
  variant?: "default" | "hero";
}

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: "예정",
  IN_PROGRESS: "경기중",
  FINISHED: "종료",
};

const STATUS_COLOR: Record<string, string> = {
  IN_PROGRESS: "#34C759",
  FINISHED: "#8E8E93",
};

export function ScoreCard({ game, variant = "default" }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const away = getTeam(game.awayTeamCode);
  const home = getTeam(game.homeTeamCode);

  const isHero = variant === "hero";
  const scheduled = game.status === "SCHEDULED";
  const live = game.status === "IN_PROGRESS";
  const finished = game.status === "FINISHED";

  // 점수는 예정 경기일 때 "-" 표시 (0:0 으로 보여 헷갈리는 걸 방지)
  const hasScore = !scheduled && (game.awayScore != null || game.homeScore != null);
  const awayScore = game.awayScore ?? 0;
  const homeScore = game.homeScore ?? 0;
  const awayWins = hasScore && awayScore > homeScore;
  const homeWins = hasScore && homeScore > awayScore;

  // 종료 경기에서만 패자를 흐리게 — 진행 중엔 둘 다 풀 컬러 (역전 가능)
  const awayScoreColor = finished && homeWins ? colors.subText : colors.text;
  const homeScoreColor = finished && awayWins ? colors.subText : colors.text;
  const awayNameColor = finished && homeWins ? colors.subText : colors.text;
  const homeNameColor = finished && awayWins ? colors.subText : colors.text;

  const statusColor = STATUS_COLOR[game.status] ?? colors.subText;
  const scoreFontSize = isHero ? 40 : 32;
  const nameFontSize = isHero ? 17 : 15;

  return (
    <Pressable
      onPress={() => router.push(`/game/${game.gameId}`)}
      style={({ pressed }) => [
        styles.card,
        {
          backgroundColor: colors.card,
          borderColor: colors.border,
          paddingVertical: isHero ? spacing.lg : spacing.md,
          paddingHorizontal: spacing.md,
        },
        pressed && { opacity: opacity.pressed },
      ]}
    >
      {/* 팀 컬러 스트라이프 — 좌(원정) / 우(홈) */}
      <View style={[styles.stripe, styles.stripeLeft, { backgroundColor: away?.color ?? colors.primary }]} />
      <View style={[styles.stripe, styles.stripeRight, { backgroundColor: home?.color ?? colors.primary }]} />

      <View style={styles.row}>
        <View style={styles.teamSide}>
          <Text style={[styles.team, { fontSize: nameFontSize, color: awayNameColor }]} numberOfLines={1}>
            {away?.shortName ?? game.awayTeamCode}
          </Text>
        </View>

        <View style={styles.scoreWrap}>
          {hasScore ? (
            <Text style={[styles.score, { fontSize: scoreFontSize }]}>
              <Text style={{ color: awayScoreColor, fontWeight: awayWins ? "800" : "700" }}>
                {awayScore}
              </Text>
              <Text style={{ color: colors.subText, fontWeight: "500" }}>{"  "}:{"  "}</Text>
              <Text style={{ color: homeScoreColor, fontWeight: homeWins ? "800" : "700" }}>
                {homeScore}
              </Text>
            </Text>
          ) : (
            <Text style={[styles.scorePlaceholder, { color: colors.subText, fontSize: scoreFontSize - 10 }]}>
              vs
            </Text>
          )}
        </View>

        <View style={[styles.teamSide, styles.teamSideRight]}>
          <Text style={[styles.team, { fontSize: nameFontSize, color: homeNameColor }]} numberOfLines={1}>
            {home?.shortName ?? game.homeTeamCode}
          </Text>
        </View>
      </View>

      <View style={styles.metaRow}>
        <Text style={[styles.meta, { color: colors.subText }]} numberOfLines={1}>
          {game.stadium ?? ""}
          {game.startTime ? ` · ${game.startTime}` : ""}
        </Text>
        <View style={[styles.statusChip, { backgroundColor: statusColor }]}>
          {live && <View style={styles.liveDot} />}
          <Text style={styles.statusText}>{STATUS_LABEL[game.status] ?? game.status}</Text>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: radius.md,
    borderWidth: border.card,
    gap: spacing.sm,
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
  row: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.sm,
  },
  teamSide: { flex: 1, alignItems: "flex-start" },
  teamSideRight: { alignItems: "flex-end" },
  team: { fontWeight: "700" },
  scoreWrap: { minWidth: 110, alignItems: "center" },
  score: { fontWeight: "700", fontVariant: ["tabular-nums"], textAlign: "center" },
  scorePlaceholder: { fontWeight: "600" },
  metaRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.sm,
  },
  meta: { fontSize: 12, flex: 1 },
  statusChip: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    paddingHorizontal: spacing.sm,
    paddingVertical: 2,
    borderRadius: radius.pill,
  },
  liveDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: "#FFFFFF",
  },
  statusText: { color: "#FFFFFF", fontSize: 11, fontWeight: "700" },
});
