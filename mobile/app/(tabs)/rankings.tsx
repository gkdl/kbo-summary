import { ActivityIndicator, RefreshControl, ScrollView, StyleSheet, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useQueries } from "@tanstack/react-query";
import { useTheme } from "../../hooks/useTheme";

import { apiClient } from "../../api/client";
import { ErrorState } from "../../components/ErrorState";
import { RankingTable } from "../../components/RankingTable";
import { useRankings } from "../../hooks/useRankings";
import type { ApiResponse } from "../../types/game";
import type { RecentForm } from "../../types/team";

export default function RankingsScreen() {
  const { colors } = useTheme();
  const rankingsQuery = useRankings();
  const standings = rankingsQuery.data ?? [];
  const teamCodes = standings.map((s) => s.teamCode);

  // 팀별 최근 5경기를 병렬 조회한다
  const recentFormResults = useQueries({
    queries: teamCodes.map((code) => ({
      queryKey: ["recentForm", code],
      queryFn: async () => {
        const res = await apiClient.get<ApiResponse<RecentForm>>(
          `/api/teams/${code}/recent-form`,
        );
        return res.data.data;
      },
      enabled: code.length > 0,
    })),
  });

  const recentForms: Record<string, string[]> = {};
  teamCodes.forEach((code, index) => {
    const data = recentFormResults[index]?.data;
    if (data) recentForms[code] = data.recentForm.slice(0, 5);
  });

  if (rankingsQuery.isLoading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
        <View style={styles.center}>
          <ActivityIndicator color={colors.primary} />
        </View>
      </SafeAreaView>
    );
  }

  if (rankingsQuery.isError) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
        <ErrorState onRetry={() => rankingsQuery.refetch()} />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      <ScrollView
        contentContainerStyle={styles.content}
        refreshControl={
          <RefreshControl
            refreshing={rankingsQuery.isFetching && !rankingsQuery.isLoading}
            onRefresh={() => rankingsQuery.refetch()}
            tintColor={colors.primary}
          />
        }
      >
        <RankingTable standings={standings} recentForms={recentForms} />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 12 },
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
});
