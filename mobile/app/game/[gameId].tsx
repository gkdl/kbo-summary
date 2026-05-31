import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
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
import { getTeam } from "../../constants/teams";
import { useGameDetail } from "../../hooks/useGameDetail";
import { useGameSummary } from "../../hooks/useGameSummary";
import { useHeadToHead } from "../../hooks/useHeadToHead";

export default function GameDetailScreen() {
  const { colors } = useTheme();
  const router = useRouter();
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
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <ActivityIndicator color={colors.primary} />
      </View>
    );
  }
  if (detailQuery.isError || !detail) {
    return <ErrorState onRetry={() => detailQuery.refetch()} />;
  }

  const awayTeam = getTeam(detail.game.awayTeamCode);
  const homeTeam = getTeam(detail.game.homeTeamCode);
  const isFinished = detail.game.status === "FINISHED";
  // KBO API 가 진행 중/예정 경기에 대해선 이닝별 점수·박스스코어 데이터를 주지 않아
  // 빈 표 대신 안내 카드를 표시한다.
  const showLiveDetails = isFinished;

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

      {showLiveDetails ? (
        <>
          <InningTable
            awayTeamCode={detail.game.awayTeamCode}
            homeTeamCode={detail.game.homeTeamCode}
            innings={detail.inningScores}
            awayLine={detail.awayLine}
            homeLine={detail.homeLine}
          />

          {detail.highlight ? <HighlightCard highlight={detail.highlight} /> : null}

          <BoxScoreTable
            awayTeamCode={detail.game.awayTeamCode}
            homeTeamCode={detail.game.homeTeamCode}
            awayHitters={detail.awayHitters ?? []}
            homeHitters={detail.homeHitters ?? []}
            awayPitchers={detail.awayPitchers ?? []}
            homePitchers={detail.homePitchers ?? []}
          />

          <AISummaryCard summary={summaryQuery.data ?? null} loading={summaryQuery.isLoading} />
        </>
      ) : (
        <View style={[styles.notice, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Text style={[styles.noticeIcon, { color: colors.primary }]}>ⓘ</Text>
          <Text style={[styles.noticeTitle, { color: colors.text }]}>
            {detail.game.status === "IN_PROGRESS" ? "경기 진행 중" : "경기 예정"}
          </Text>
          <Text style={[styles.noticeBody, { color: colors.subText }]}>
            이닝별 점수와 박스스코어, AI 요약은 경기 종료 후 표시됩니다.
          </Text>
        </View>
      )}

      {headToHeadQuery.data ? <HeadToHeadCard data={headToHeadQuery.data} /> : null}
      </ScrollView>
      {/* 광고는 ScrollView 밖에서 화면 하단에 고정 — 스크롤과 독립 */}
      <AdBanner />
    </View>
  );
}

const styles = StyleSheet.create({
  content: { padding: 12, gap: 16 },
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
  teamLinkRow: { flexDirection: "row", gap: 8 },
  teamLink: {
    flex: 1,
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderWidth: 1,
    borderRadius: 8,
    alignItems: "center",
  },
  teamLinkText: { fontSize: 13, fontWeight: "600" },
  notice: {
    padding: 20,
    borderRadius: 10,
    borderWidth: 1,
    alignItems: "center",
    gap: 8,
  },
  noticeIcon: { fontSize: 28, fontWeight: "700" },
  noticeTitle: { fontSize: 15, fontWeight: "700" },
  noticeBody: { fontSize: 13, textAlign: "center", lineHeight: 20 },
});
