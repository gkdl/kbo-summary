import { useState } from "react";
import {
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
import { SafeAreaView } from "react-native-safe-area-context";

import { DateNavBar } from "../../components/DateNavBar";
import { EmptyState } from "../../components/EmptyState";
import { ErrorState } from "../../components/ErrorState";
import { HighlightCardSkeleton } from "../../components/skeletons/HighlightCardSkeleton";
import { getTeam } from "../../constants/teams";
import { useHighlightsByDate } from "../../hooks/useHighlightsByDate";
import { useTheme } from "../../hooks/useTheme";
import type { GameHighlight } from "../../types/game";
import { toYyyymmdd } from "../../utils/date";
import { border, opacity, radius, spacing } from "../../constants/tokens";

const yesterday = () => toYyyymmdd(new Date(Date.now() - 86400_000));

export default function HighlightsScreen() {
  const { colors } = useTheme();
  // 하이라이트는 보통 경기 종료 후 publish 되므로 기본값은 어제
  const [selectedDate, setSelectedDate] = useState(yesterday);

  const query = useHighlightsByDate(selectedDate);

  const dateBar = (
    <DateNavBar
      value={selectedDate}
      onChange={setSelectedDate}
      defaultDate={yesterday}
      maxDate={new Date()}
    />
  );

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      {dateBar}
      {query.isLoading ? (
        <View style={styles.listContent}>
          <HighlightCardSkeleton />
          <HighlightCardSkeleton />
          <HighlightCardSkeleton />
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
            <EmptyState icon="🎬" message="아직 하이라이트가 없어요" />
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
        { backgroundColor: colors.card, borderColor: colors.border },
        pressed && { opacity: opacity.pressed },
      ]}
    >
      <View style={styles.thumbWrap}>
        <Image source={{ uri: thumbUrl }} style={styles.thumb} resizeMode="cover" />
        {/* 영상임을 알리는 ▶ 오버레이 */}
        <View style={styles.playOverlay}>
          <View style={styles.playCircle}>
            <View style={styles.playTriangle} />
          </View>
        </View>
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
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
  listContent: { padding: spacing.md, gap: spacing.sm + 2 },
  card: { borderRadius: radius.md, borderWidth: border.card, overflow: "hidden" },
  thumbWrap: { width: "100%", aspectRatio: 16 / 9, backgroundColor: "#000000" },
  thumb: { width: "100%", height: "100%" },
  playOverlay: {
    ...StyleSheet.absoluteFillObject,
    alignItems: "center",
    justifyContent: "center",
  },
  playCircle: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: "rgba(0,0,0,0.55)",
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1.5,
    borderColor: "rgba(255,255,255,0.85)",
  },
  // border 트릭으로 ▶ 모양 — 좌측 변만 색을 채우고 나머지는 투명
  playTriangle: {
    width: 0,
    height: 0,
    marginLeft: 4,
    borderTopWidth: 9,
    borderBottomWidth: 9,
    borderLeftWidth: 14,
    borderTopColor: "transparent",
    borderBottomColor: "transparent",
    borderLeftColor: "#FFFFFF",
  },
  meta: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm + 2, gap: spacing.xs },
  teamsRow: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
  teamName: { fontSize: 14, fontWeight: "700", flex: 1, textAlign: "center" },
  score: { fontSize: 14, fontWeight: "700", marginHorizontal: spacing.sm },
  title: { fontSize: 12 },
});
