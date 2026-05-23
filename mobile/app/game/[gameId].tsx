import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useTheme } from "@react-navigation/native";

import { AISummaryCard } from "../../components/AISummaryCard";
import { BoxScoreTable } from "../../components/BoxScoreTable";
import { ErrorState } from "../../components/ErrorState";
import { HeadToHeadCard } from "../../components/HeadToHeadCard";
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
  const summaryQuery = useGameSummary(id);
  const detail = detailQuery.data;
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

  return (
    <ScrollView style={{ backgroundColor: colors.background }} contentContainerStyle={styles.content}>
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

      {headToHeadQuery.data ? <HeadToHeadCard data={headToHeadQuery.data} /> : null}

      <InningTable
        awayTeamCode={detail.game.awayTeamCode}
        homeTeamCode={detail.game.homeTeamCode}
        innings={detail.inningScores}
        awayLine={detail.awayLine}
        homeLine={detail.homeLine}
      />

      <AISummaryCard summary={summaryQuery.data ?? null} loading={summaryQuery.isLoading} />

      {/* BoxScore: API에 hitter/pitcher 데이터가 노출되면 채워진다. 현재는 헤더만 표시 */}
      <BoxScoreTable hitters={[]} pitchers={[]} />
    </ScrollView>
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
});
