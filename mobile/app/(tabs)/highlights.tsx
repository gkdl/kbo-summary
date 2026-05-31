import { useState } from "react";
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Image,
  Linking,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from "react-native";
import DateTimePickerModal from "react-native-modal-datetime-picker";
import { SafeAreaView } from "react-native-safe-area-context";

import { EmptyState } from "../../components/EmptyState";
import { ErrorState } from "../../components/ErrorState";
import { getTeam } from "../../constants/teams";
import { useHighlightsByDate } from "../../hooks/useHighlightsByDate";
import { useTheme } from "../../hooks/useTheme";
import type { GameHighlight } from "../../types/game";

// 홈 탭과 동일한 유틸 — 추후 공통 모듈로 빼도 좋음
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

export default function HighlightsScreen() {
  const { colors } = useTheme();
  // 하이라이트는 보통 경기 종료 후 publish 되므로 기본값은 어제
  const [selectedDate, setSelectedDate] = useState(() => toYyyymmdd(new Date(Date.now() - 86400_000)));
  const [pickerVisible, setPickerVisible] = useState(false);

  const query = useHighlightsByDate(selectedDate);

  const dateBar = (
    <View style={[styles.dateBar, { borderBottomColor: colors.border }]}>
      <Pressable onPress={() => setSelectedDate((d) => shiftDays(d, -1))}>
        <Text style={[styles.navButton, { color: colors.primary }]}>← 어제</Text>
      </Pressable>
      <Pressable
        onPress={() => setPickerVisible(true)}
        onLongPress={() => setSelectedDate(toYyyymmdd(new Date(Date.now() - 86400_000)))}
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
        minimumDate={new Date(new Date().getFullYear() - 1, 0, 1)}
        maximumDate={new Date()}
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

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      {dateBar}
      {query.isLoading ? (
        <View style={styles.center}>
          <ActivityIndicator color={colors.primary} />
        </View>
      ) : query.isError ? (
        <ErrorState onRetry={() => query.refetch()} />
      ) : (
        <FlatList
          data={query.data ?? []}
          keyExtractor={(item) => item.gameId}
          contentContainerStyle={styles.listContent}
          ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
          refreshControl={
            <RefreshControl
              refreshing={query.isFetching && !query.isLoading}
              onRefresh={() => query.refetch()}
              tintColor={colors.primary}
            />
          }
          ListEmptyComponent={
            <EmptyState message="이 날짜의 하이라이트가 없습니다" />
          }
          renderItem={({ item }) => <HighlightRow data={item} />}
        />
      )}
    </SafeAreaView>
  );
}

interface RowProps {
  data: GameHighlight;
}

function HighlightRow({ data }: RowProps) {
  const { colors } = useTheme();
  const away = getTeam(data.awayTeamCode);
  const home = getTeam(data.homeTeamCode);
  const thumbUrl = `https://i.ytimg.com/vi/${data.highlight.youtubeVideoId}/hqdefault.jpg`;
  const videoUrl = `https://www.youtube.com/watch?v=${data.highlight.youtubeVideoId}`;

  const openVideo = async () => {
    const ok = await Linking.canOpenURL(videoUrl);
    if (ok) {
      await Linking.openURL(videoUrl);
    } else {
      Alert.alert("영상을 열 수 없습니다", videoUrl);
    }
  };

  return (
    <Pressable
      onPress={openVideo}
      style={({ pressed }) => [
        styles.card,
        { backgroundColor: colors.card, borderColor: colors.border, opacity: pressed ? 0.85 : 1 },
      ]}
    >
      <View style={styles.thumbWrap}>
        <Image source={{ uri: thumbUrl }} style={styles.thumb} resizeMode="cover" />
      </View>

      <View style={styles.meta}>
        <View style={styles.teamsRow}>
          <Text style={[styles.teamName, { color: colors.text }]}>
            {away?.name ?? data.awayTeamCode}
          </Text>
          <Text style={[styles.score, { color: colors.text }]}>
            {data.awayScore ?? "-"} : {data.homeScore ?? "-"}
          </Text>
          <Text style={[styles.teamName, { color: colors.text }]}>
            {home?.name ?? data.homeTeamCode}
          </Text>
        </View>
        {data.highlight.title ? (
          <Text
            style={[styles.title, { color: colors.subText }]}
            numberOfLines={2}
          >
            {data.highlight.title}
          </Text>
        ) : null}
      </View>
    </Pressable>
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
  card: { borderRadius: 10, borderWidth: 1, overflow: "hidden" },
  thumbWrap: { width: "100%", aspectRatio: 16 / 9, backgroundColor: "#000000" },
  thumb: { width: "100%", height: "100%" },
  meta: { paddingHorizontal: 12, paddingVertical: 10, gap: 4 },
  teamsRow: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
  teamName: { fontSize: 14, fontWeight: "700", flex: 1, textAlign: "center" },
  score: { fontSize: 14, fontWeight: "700", marginHorizontal: 8 },
  title: { fontSize: 12 },
});
