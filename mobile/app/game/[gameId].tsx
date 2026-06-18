import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useTheme } from "../../hooks/useTheme";

import { AdBanner } from "../../components/AdBanner";
import { AISummaryCard } from "../../components/AISummaryCard";
import { BoxScoreTable } from "../../components/BoxScoreTable";
import { ErrorState } from "../../components/ErrorState";
import { GameScoreHero } from "../../components/GameScoreHero";
import { HeadToHeadCard } from "../../components/HeadToHeadCard";
import { HighlightCard } from "../../components/HighlightCard";
import { InningTable } from "../../components/InningTable";
import { ScoreCardSkeleton } from "../../components/skeletons/ScoreCardSkeleton";
import { TableSkeleton } from "../../components/skeletons/TableSkeleton";
import { spacing } from "../../constants/tokens";
import { getTeam } from "../../constants/teams";
import { useGameDetail } from "../../hooks/useGameDetail";
import { useGameSummary } from "../../hooks/useGameSummary";
import { useHeadToHead } from "../../hooks/useHeadToHead";

export default function GameDetailScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { gameId } = useLocalSearchParams<{ gameId: string }>();
  const id = gameId ?? "";

  const detailQuery = useGameDetail(id);
  const detail = detailQuery.data;
  // AI 요약은 종료된 경기만 — 진행 중/예정 경기는 백엔드가 500 을 반환하므로 호출 자체를 차단
  const summaryQuery = useGameSummary(id, detail?.game.status === "FINISHED");
  const headToHeadQuery = useHeadToHead(
    detail?.game.awayTeamCode ?? "",
    detail?.game.homeTeamCode ?? "",
  );

  if (detailQuery.isLoading) {
    return (
      <View style={[styles.skeletonWrap, { backgroundColor: colors.background }]}>
        <ScoreCardSkeleton variant="hero" />
        <TableSkeleton rows={4} />
      </View>
    );
  }
  if (detailQuery.isError || !detail) {
    return <ErrorState onRetry={() => detailQuery.refetch()} />;
  }

  const awayTeam = getTeam(detail.game.awayTeamCode);
  const homeTeam = getTeam(detail.game.homeTeamCode);
  const status = detail.game.status;
  const isFinished = status === "FINISHED";
  // KBO 는 종료된 경기만 이닝별 점수·박스스코어를 제공한다(진행 중/예정 경기엔 데이터 없음).
  // 진행 중 경기의 라이브 점수는 상단 히어로에 표시되고, 이닝별 상세는 종료 후 표시한다.
  const showScoreDetails = isFinished;

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScrollView contentContainerStyle={styles.content}>
      <GameScoreHero game={detail.game} />

      <View style={styles.teamLinkRow}>
        <Pressable
          onPress={() => router.push(`/team/${detail.game.awayTeamCode}`)}
          style={({ pressed }) => [
            styles.teamLink,
            { borderColor: awayTeam?.color ?? colors.border, opacity: pressed ? 0.6 : 1 },
          ]}
        >
          <Text style={[styles.teamLinkText, { color: colors.text }]}>
            {awayTeam?.name ?? detail.game.awayTeamCode} →
          </Text>
        </Pressable>
        <Pressable
          onPress={() => router.push(`/team/${detail.game.homeTeamCode}`)}
          style={({ pressed }) => [
            styles.teamLink,
            { borderColor: homeTeam?.color ?? colors.border, opacity: pressed ? 0.6 : 1 },
          ]}
        >
          <Text style={[styles.teamLinkText, { color: colors.text }]}>
            {homeTeam?.name ?? detail.game.homeTeamCode} →
          </Text>
        </Pressable>
      </View>

      {showScoreDetails ? (
        <>
          <InningTable
            awayTeamCode={detail.game.awayTeamCode}
            homeTeamCode={detail.game.homeTeamCode}
            innings={detail.inningScores}
            awayLine={detail.awayLine}
            homeLine={detail.homeLine}
          />

          {/* AI 요약은 이닝 결과 바로 아래 — 흐름을 한눈에 본 직후 요약을 읽을 수 있게 */}
          <AISummaryCard summary={summaryQuery.data ?? null} loading={summaryQuery.isLoading} />

          {detail.highlight ? <HighlightCard highlight={detail.highlight} /> : null}

          <BoxScoreTable
            awayTeamCode={detail.game.awayTeamCode}
            homeTeamCode={detail.game.homeTeamCode}
            awayHitters={detail.awayHitters ?? []}
            homeHitters={detail.homeHitters ?? []}
            awayPitchers={detail.awayPitchers ?? []}
            homePitchers={detail.homePitchers ?? []}
          />
        </>
      ) : (
        <View style={[styles.notice, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Text style={[styles.noticeIcon, { color: colors.primary }]}>ⓘ</Text>
          <Text style={[styles.noticeTitle, { color: colors.text }]}>
            {status === "IN_PROGRESS" ? "경기 진행 중" : "경기 예정"}
          </Text>
          <Text style={[styles.noticeBody, { color: colors.subText }]}>
            상단에 실시간 점수가 표시됩니다. 이닝별 점수·박스스코어·AI 요약은 경기 종료 후 제공됩니다.
          </Text>
        </View>
      )}

      {headToHeadQuery.data ? <HeadToHeadCard data={headToHeadQuery.data} /> : null}
      </ScrollView>
      {/* 광고는 ScrollView 밖에서 화면 하단에 고정 — 스크롤과 독립.
          시스템 홈바에 가리지 않게 safe-area 하단 inset 적용 */}
      <View style={{ paddingBottom: insets.bottom, backgroundColor: colors.background }}>
        <AdBanner />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  content: { padding: spacing.md, gap: spacing.lg },
  skeletonWrap: { flex: 1, padding: spacing.md, gap: spacing.lg },
  teamLinkRow: { flexDirection: "row", gap: spacing.sm },
  teamLink: {
    flex: 1,
    paddingVertical: 10,
    paddingHorizontal: spacing.md,
    borderWidth: 1,
    borderRadius: 8,
    alignItems: "center",
  },
  teamLinkText: { fontSize: 13, fontWeight: "600" },
  notice: {
    padding: spacing.xl - 4,
    borderRadius: 10,
    borderWidth: 1,
    alignItems: "center",
    gap: spacing.sm,
  },
  noticeIcon: { fontSize: 28, fontWeight: "700" },
  noticeTitle: { fontSize: 15, fontWeight: "700" },
  noticeBody: { fontSize: 13, textAlign: "center", lineHeight: 20 },
});
