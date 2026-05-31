import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { useQuery } from "@tanstack/react-query";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useTheme } from "../../hooks/useTheme";

import { apiClient } from "../../api/client";
import { AdBanner } from "../../components/AdBanner";
import { ErrorState } from "../../components/ErrorState";
import { PlayerProfileHeader } from "../../components/PlayerProfileHeader";
import { PlayerStatTable } from "../../components/PlayerStatTable";
import { getTeam } from "../../constants/teams";
import { usePlayer } from "../../hooks/usePlayer";
import type { ApiResponse } from "../../types/game";
import type { PlayerStat } from "../../types/player";

export default function PlayerDetailScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { playerId } = useLocalSearchParams<{ playerId: string }>();
  const id = playerId ?? "";

  const profileQuery = usePlayer(id);

  // STEP 11에 usePlayerStats 훅이 없어 인라인 조회
  const statQuery = useQuery({
    queryKey: ["playerStats", id],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PlayerStat>>(`/api/players/${id}/stats`);
      return res.data.data;
    },
    enabled: id.length > 0,
  });

  if (profileQuery.isLoading) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <ActivityIndicator color={colors.primary} />
      </View>
    );
  }
  if (profileQuery.isError || !profileQuery.data) {
    return <ErrorState onRetry={() => profileQuery.refetch()} />;
  }
  const profile = profileQuery.data;
  const team = profile.teamCode ? getTeam(profile.teamCode) : undefined;

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScrollView contentContainerStyle={styles.content}>
      <PlayerProfileHeader profile={profile} />

      {profile.teamCode ? (
        <Pressable
          onPress={() => router.push(`/team/${profile.teamCode}`)}
          style={({ pressed }) => [
            styles.teamLink,
            { borderColor: team?.color ?? colors.border, opacity: pressed ? 0.6 : 1 },
          ]}
        >
          <Text style={[styles.teamLinkText, { color: colors.text }]}>
            {team?.name ?? profile.teamCode} 팀 상세 →
          </Text>
        </Pressable>
      ) : null}

      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.text }]}>시즌 기록</Text>
        {statQuery.isLoading ? (
          <ActivityIndicator color={colors.primary} />
        ) : statQuery.data ? (
          <PlayerStatTable stat={statQuery.data} />
        ) : (
          <Text style={{ color: colors.subText }}>시즌 기록을 불러올 수 없습니다</Text>
        )}
      </View>

</ScrollView>
      {/* 화면 하단에 항상 고정 */}
      <AdBanner />
    </View>
  );
}

const styles = StyleSheet.create({
  content: { padding: 12, gap: 16 },
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
  teamLink: {
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderWidth: 1,
    borderRadius: 8,
    alignItems: "center",
  },
  teamLinkText: { fontSize: 13, fontWeight: "600" },
  section: { gap: 8 },
  sectionTitle: { fontSize: 14, fontWeight: "700" },
});
