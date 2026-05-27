import { useState } from "react";
import { ActivityIndicator, FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from "react-native";
import DateTimePickerModal from "react-native-modal-datetime-picker";
import { SafeAreaView } from "react-native-safe-area-context";
import { useTheme } from "../../hooks/useTheme";

import { EmptyState } from "../../components/EmptyState";
import { ErrorState } from "../../components/ErrorState";
import { RecentFormDots } from "../../components/RecentFormDots";
import { ScoreCard } from "../../components/ScoreCard";
import { getTeam } from "../../constants/teams";
import { useGames } from "../../hooks/useGames";
import { useRecentForm } from "../../hooks/useRecentForm";
import { useAppStore } from "../../store/useAppStore";

function toYyyymmdd(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}${m}${day}`;
}

function parseYyyymmdd(yyyymmdd: string): Date {
  const y = Number(yyyymmdd.slice(0, 4));
  const m = Number(yyyymmdd.slice(4, 6)) - 1;
  const d = Number(yyyymmdd.slice(6, 8));
  return new Date(y, m, d);
}

function shiftDays(yyyymmdd: string, delta: number): string {
  const y = Number(yyyymmdd.slice(0, 4));
  const m = Number(yyyymmdd.slice(4, 6)) - 1;
  const d = Number(yyyymmdd.slice(6, 8));
  return toYyyymmdd(new Date(y, m, d + delta));
}

function displayDate(yyyymmdd: string): string {
  return `${yyyymmdd.slice(0, 4)}.${yyyymmdd.slice(4, 6)}.${yyyymmdd.slice(6, 8)}`;
}

export default function HomeScreen() {
  const { colors } = useTheme();
  const [selectedDate, setSelectedDate] = useState(toYyyymmdd(new Date()));
  const [pickerVisible, setPickerVisible] = useState(false);
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
    <View style={[styles.dateBar, { borderBottomColor: colors.border }]}>
      <Pressable onPress={() => setSelectedDate((d) => shiftDays(d, -1))}>
        <Text style={[styles.navButton, { color: colors.primary }]}>← 어제</Text>
      </Pressable>
      <Pressable
        onPress={() => setPickerVisible(true)}
        onLongPress={() => setSelectedDate(toYyyymmdd(new Date()))}
      >
        <Text style={[styles.dateText, { color: colors.text }]}>{displayDate(selectedDate)}</Text>
      </Pressable>
      <Pressable onPress={() => setSelectedDate((d) => shiftDays(d, 1))}>
        <Text style={[styles.navButton, { color: colors.primary }]}>다음 →</Text>
      </Pressable>

      <DateTimePickerModal
        isVisible={pickerVisible}
        mode="date"
        date={parseYyyymmdd(selectedDate)}
        // 시즌 종료 후 너무 먼 미래까지 가지 않도록 1년 범위로 제한
        minimumDate={new Date(new Date().getFullYear() - 1, 0, 1)}
        maximumDate={new Date(new Date().getFullYear() + 1, 11, 31)}
        locale="ko-KR"
        confirmTextIOS="선택"
        cancelTextIOS="취소"
        onConfirm={(date) => {
          setSelectedDate(toYyyymmdd(date));
          setPickerVisible(false);
        }}
        onCancel={() => setPickerVisible(false)}
      />
    </View>
  );

  if (gamesQuery.isLoading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
        {dateBar}
        <View style={styles.center}>
          <ActivityIndicator color={colors.primary} />
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
              <ScoreCard game={myGame} />
            </View>
          ) : null
        }
        ListEmptyComponent={
          !myGame ? <EmptyState message="이 날짜의 경기가 없습니다" /> : null
        }
        renderItem={({ item }) => <ScoreCard game={item} />}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  dateBar: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  navButton: { fontSize: 14, fontWeight: "600" },
  dateText: { fontSize: 15, fontWeight: "700" },
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
  listContent: { padding: 12, gap: 10 },
  myTeamWrap: {
    borderWidth: 2,
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
    gap: 10,
  },
  myTeamHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  myTeamBadgeWrap: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
  },
  myTeamBadge: { color: "#FFFFFF", fontSize: 11, fontWeight: "700" },
});
