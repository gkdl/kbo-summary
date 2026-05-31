import { useState } from "react";
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { useLocalSearchParams } from "expo-router";
import { useTheme } from "../../hooks/useTheme";

import { AdBanner } from "../../components/AdBanner";
import { EmptyState } from "../../components/EmptyState";
import { ErrorState } from "../../components/ErrorState";
import { HeadToHeadCard } from "../../components/HeadToHeadCard";
import { TeamCard } from "../../components/TeamCard";
import { TeamRosterList } from "../../components/TeamRosterList";
import { TeamSelector } from "../../components/TeamSelector";
import { TeamStatTable } from "../../components/TeamStatTable";
import { useHeadToHead } from "../../hooks/useHeadToHead";
import { useRankings } from "../../hooks/useRankings";
import { useRecentForm } from "../../hooks/useRecentForm";
import { useTeam } from "../../hooks/useTeam";
import { useTeamRoster } from "../../hooks/useTeamRoster";

type Tab = "overview" | "roster" | "headToHead";

export default function TeamDetailScreen() {
  const { colors } = useTheme();
  const { teamCode } = useLocalSearchParams<{ teamCode: string }>();
  const code = teamCode ?? "";

  const [tab, setTab] = useState<Tab>("overview");
  const [opponent, setOpponent] = useState<string | null>(null);

  const teamQuery = useTeam(code);
  const recentFormQuery = useRecentForm(code);
  const rosterQuery = useTeamRoster(code);
  const rankingsQuery = useRankings();
  const headToHeadQuery = useHeadToHead(code, opponent ?? "");

  if (teamQuery.isLoading) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <ActivityIndicator color={colors.primary} />
      </View>
    );
  }
  if (teamQuery.isError || !teamQuery.data) {
    return <ErrorState onRetry={() => teamQuery.refetch()} />;
  }
  const team = teamQuery.data;
  const standing = rankingsQuery.data?.find((s) => s.teamCode === code);

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScrollView contentContainerStyle={styles.content}>
      <TeamCard
        team={team}
        recentForm={recentFormQuery.data?.recentForm.slice(0, 10)}
      />

      <View style={[styles.tabBar, { borderColor: colors.border }]}>
        <TabButton label="개요" active={tab === "overview"} onPress={() => setTab("overview")} colors={colors} />
        <TabButton label="로스터" active={tab === "roster"} onPress={() => setTab("roster")} colors={colors} />
        <TabButton label="상대전적" active={tab === "headToHead"} onPress={() => setTab("headToHead")} colors={colors} />
      </View>

      {tab === "overview" ? (
        standing ? (
          <TeamStatTable
            stats={{
              teamCode: standing.teamCode,
              season: standing.season,
              rank: standing.rank,
              wins: standing.wins,
              losses: standing.losses,
              draws: standing.draws,
              winRate: standing.winRate,
              gamesBehind: standing.gamesBehind,
            }}
          />
        ) : (
          <EmptyState message="시즌 통계가 없습니다" />
        )
      ) : null}

      {tab === "roster" ? (
        rosterQuery.isLoading ? (
          <ActivityIndicator color={colors.primary} />
        ) : rosterQuery.data ? (
          <TeamRosterList roster={rosterQuery.data} />
        ) : (
          <EmptyState message="로스터 정보가 없습니다" />
        )
      ) : null}

      {tab === "headToHead" ? (
        <View style={{ gap: 12 }}>
          <Text style={[styles.label, { color: colors.text }]}>상대팀 선택</Text>
          <TeamSelector
            selectedCode={opponent}
            onSelect={(picked) => setOpponent(picked === code ? null : picked)}
          />
          {opponent ? (
            headToHeadQuery.isLoading ? (
              <ActivityIndicator color={colors.primary} />
            ) : headToHeadQuery.data ? (
              <HeadToHeadCard data={headToHeadQuery.data} />
            ) : null
          ) : (
            <Text style={{ color: colors.subText }}>
              상대팀을 선택하면 시즌 상대전적이 표시됩니다
            </Text>
          )}
        </View>
      ) : null}
      </ScrollView>
      {/* 화면 하단에 항상 고정 */}
      <AdBanner />
    </View>
  );
}

interface TabButtonProps {
  label: string;
  active: boolean;
  onPress: () => void;
  colors: { primary: string; text: string };
}

function TabButton({ label, active, onPress, colors }: TabButtonProps) {
  return (
    <Pressable
      onPress={onPress}
      style={[
        styles.tabButton,
        active ? { borderBottomColor: colors.primary } : { borderBottomColor: "transparent" },
      ]}
    >
      <Text
        style={[
          styles.tabText,
          { color: active ? colors.primary : colors.text, opacity: active ? 1 : 0.6 },
        ]}
      >
        {label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  content: { padding: 12, gap: 16 },
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
  tabBar: { flexDirection: "row", borderBottomWidth: 1 },
  tabButton: { flex: 1, paddingVertical: 12, alignItems: "center", borderBottomWidth: 2 },
  tabText: { fontSize: 14, fontWeight: "600" },
  label: { fontSize: 14, fontWeight: "600" },
});
