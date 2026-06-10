import { useState } from "react";
import { FlatList, RefreshControl, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useTheme } from "../../hooks/useTheme";

import { DateNavBar } from "../../components/DateNavBar";
import { EmptyState } from "../../components/EmptyState";
import { ErrorState } from "../../components/ErrorState";
import { RecentFormDots } from "../../components/RecentFormDots";
import { ScoreCard } from "../../components/ScoreCard";
import { ScoreCardSkeleton } from "../../components/skeletons/ScoreCardSkeleton";
import { getTeam } from "../../constants/teams";
import { useGames } from "../../hooks/useGames";
import { useRecentForm } from "../../hooks/useRecentForm";
import { useAppStore } from "../../store/useAppStore";
import { toYyyymmdd } from "../../utils/date";
import { border, radius, spacing } from "../../constants/tokens";

export default function HomeScreen() {
  const { colors } = useTheme();
  const [selectedDate, setSelectedDate] = useState(toYyyymmdd(new Date()));
  const myTeam = useAppStore((state) => state.myTeam);

  const gamesQuery = useGames(selectedDate);
  const recentFormQuery = useRecentForm(myTeam ?? "");

  const games = gamesQuery.data ?? [];
  const myGame = myTeam
    ? games.find((g) => g.homeTeamCode === myTeam || g.awayTeamCode === myTeam)
    : undefined;
  const otherGames = myGame ? games.filter((g) => g.gameId !== myGame.gameId) : games;
  const meta = myTeam ? getTeam(myTeam) : undefined;

  const dateBar = (
    <DateNavBar
      value={selectedDate}
      onChange={setSelectedDate}
      defaultDate={() => toYyyymmdd(new Date())}
    />
  );

  if (gamesQuery.isLoading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
        {dateBar}
        <View style={styles.listContent}>
          <ScoreCardSkeleton variant="hero" />
          <ScoreCardSkeleton />
          <ScoreCardSkeleton />
          <ScoreCardSkeleton />
        </View>
      </SafeAreaView>
    );
  }

  if (gamesQuery.isError) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
        {dateBar}
        <ErrorState onRetry={() => gamesQuery.refetch()} />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      {dateBar}
      <FlatList
        data={otherGames}
        keyExtractor={(g) => g.gameId}
        contentContainerStyle={styles.listContent}
        ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
        refreshControl={
          <RefreshControl
            refreshing={gamesQuery.isFetching && !gamesQuery.isLoading}
            onRefresh={() => gamesQuery.refetch()}
            tintColor={colors.primary}
          />
        }
        ListHeaderComponent={
          myGame && meta ? (
            <View style={[styles.myTeamWrap, { borderColor: meta.color, backgroundColor: colors.card }]}>
              <View style={styles.myTeamHeader}>
                <View style={[styles.myTeamBadgeWrap, { backgroundColor: meta.color }]}>
                  <Text style={styles.myTeamBadge}>MY TEAM</Text>
                </View>
                {recentFormQuery.data && recentFormQuery.data.recentForm.length > 0 ? (
                  <RecentFormDots recentForm={recentFormQuery.data.recentForm.slice(0, 5)} />
                ) : null}
              </View>
              <ScoreCard game={myGame} variant="hero" />
            </View>
          ) : null
        }
        ListEmptyComponent={
          !myGame ? <EmptyState icon="⚾" message="이 날짜에는 경기가 없어요" /> : null
        }
        renderItem={({ item }) => <ScoreCard game={item} />}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
  listContent: { padding: spacing.md, gap: spacing.sm + 2 },
  myTeamWrap: {
    borderWidth: border.emphasis,
    borderRadius: radius.md,
    padding: spacing.md,
    marginBottom: spacing.md,
    gap: spacing.sm + 2,
  },
  myTeamHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  myTeamBadgeWrap: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: 4,
  },
  myTeamBadge: { color: "#FFFFFF", fontSize: 11, fontWeight: "700" },
});
