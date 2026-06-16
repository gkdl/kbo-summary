import { useState } from "react";
import { FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useRouter } from "expo-router";

import { EmptyState } from "../../components/EmptyState";
import { ErrorState } from "../../components/ErrorState";
import { PostCard, PostSeparator } from "../../components/PostCard";
import { TableSkeleton } from "../../components/skeletons/TableSkeleton";
import { TEAMS } from "../../constants/teams";
import { border, opacity, radius, spacing } from "../../constants/tokens";
import { useAuth } from "../../hooks/useAuth";
import { usePosts } from "../../hooks/usePosts";
import { useTheme } from "../../hooks/useTheme";
import { useAppStore } from "../../store/useAppStore";
import type { PostListItem } from "../../types/community";

export default function CommunityScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { isAuthed } = useAuth();
  const myTeam = useAppStore((s) => s.myTeam);
  // 기본 게시판: 마이팀 있으면 그 팀, 없으면 첫 구단
  const [team, setTeam] = useState<string>(myTeam ?? TEAMS[0].code);
  const [sort, setSort] = useState<"latest" | "popular">("latest");

  const query = usePosts({ team, sort });
  const posts: PostListItem[] = query.data?.pages.flatMap((p) => p?.items ?? []) ?? [];

  const SORTS: { key: "popular" | "latest"; label: string }[] = [
    { key: "popular", label: "인기글" },
    { key: "latest", label: "최신글" },
  ];

  const onWrite = () => {
    if (!isAuthed) {
      router.push("/login");
      return;
    }
    router.push(`/post/write?team=${team}`);
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      <View style={[styles.chipRow, { borderBottomColor: colors.border }]}>
        <FlatList
          horizontal
          showsHorizontalScrollIndicator={false}
          data={TEAMS}
          keyExtractor={(t) => t.code}
          contentContainerStyle={styles.chipContent}
          renderItem={({ item }) => {
            const active = team === item.code;
            return (
              <Pressable
                onPress={() => setTeam(item.code)}
                style={({ pressed }) => [
                  styles.chip,
                  {
                    backgroundColor: active ? item.color : colors.card,
                    borderColor: active ? item.color : colors.border,
                    opacity: pressed ? opacity.pressed : 1,
                  },
                ]}
              >
                <Text style={[styles.chipText, { color: active ? "#FFFFFF" : colors.text }]}>
                  {item.shortName}
                </Text>
              </Pressable>
            );
          }}
        />
      </View>

      <View style={[styles.sortRow, { borderBottomColor: colors.border }]}>
        {SORTS.map((s) => {
          const active = sort === s.key;
          return (
            <Pressable key={s.key} onPress={() => setSort(s.key)} style={styles.sortTab}>
              <Text
                style={[
                  styles.sortText,
                  { color: active ? colors.primary : colors.subText, fontWeight: active ? "700" : "500" },
                ]}
              >
                {s.label}
              </Text>
              {active ? <View style={[styles.sortUnderline, { backgroundColor: colors.primary }]} /> : null}
            </Pressable>
          );
        })}
      </View>

      {query.isLoading ? (
        <View style={styles.listWrap}>
          <TableSkeleton rows={6} />
        </View>
      ) : query.isError ? (
        <ErrorState onRetry={() => query.refetch()} />
      ) : (
        <FlatList
          data={posts}
          keyExtractor={(p) => String(p.postId)}
          contentContainerStyle={styles.listContent}
          ItemSeparatorComponent={PostSeparator}
          refreshControl={
            <RefreshControl
              refreshing={query.isFetching && !query.isFetchingNextPage}
              onRefresh={() => query.refetch()}
              tintColor={colors.primary}
            />
          }
          onEndReachedThreshold={0.4}
          onEndReached={() => {
            if (query.hasNextPage && !query.isFetchingNextPage) query.fetchNextPage();
          }}
          ListEmptyComponent={<EmptyState icon="💬" message="아직 글이 없어요" hint="첫 글을 남겨보세요" />}
          renderItem={({ item }) => <PostCard post={item} />}
        />
      )}

      <Pressable
        onPress={onWrite}
        accessibilityLabel="글쓰기"
        style={({ pressed }) => [styles.fab, { backgroundColor: colors.primary, opacity: pressed ? opacity.pressed : 1 }]}
      >
        <Text style={styles.fabIcon}>✏️</Text>
      </Pressable>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  chipRow: { borderBottomWidth: border.hairline },
  chipContent: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, gap: spacing.sm },
  chip: { paddingHorizontal: spacing.md, paddingVertical: 7, borderRadius: radius.pill, borderWidth: border.card },
  chipText: { fontSize: 13, fontWeight: "600" },
  sortRow: { flexDirection: "row", paddingHorizontal: spacing.md, borderBottomWidth: border.hairline },
  sortTab: { paddingVertical: spacing.sm + 2, marginRight: spacing.lg, alignItems: "center" },
  sortText: { fontSize: 14 },
  sortUnderline: { position: "absolute", bottom: -border.hairline, left: 0, right: 0, height: 2 },
  listContent: { paddingHorizontal: spacing.md, paddingBottom: 90 },
  listWrap: { padding: spacing.md },
  fab: {
    position: "absolute",
    right: spacing.lg,
    bottom: spacing.lg,
    width: 52,
    height: 52,
    borderRadius: radius.pill,
    alignItems: "center",
    justifyContent: "center",
  },
  fabIcon: { fontSize: 22 },
});
