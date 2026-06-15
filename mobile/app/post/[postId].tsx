import { Alert, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";

import { ErrorState } from "../../components/ErrorState";
import { TableSkeleton } from "../../components/skeletons/TableSkeleton";
import { getTeam } from "../../constants/teams";
import { border, radius, spacing } from "../../constants/tokens";
import { usePost } from "../../hooks/usePosts";
import { useDeletePost } from "../../hooks/usePostMutations";
import { useTheme } from "../../hooks/useTheme";
import { relativeTime } from "../../utils/relativeTime";

export default function PostDetailScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { postId } = useLocalSearchParams<{ postId: string }>();
  const id = postId ?? "";

  const query = usePost(id);
  const deletePost = useDeletePost();
  const post = query.data;
  const team = post?.teamCode ? getTeam(post.teamCode) : undefined;

  const onDelete = () =>
    Alert.alert("글 삭제", "이 글을 삭제할까요?", [
      { text: "취소", style: "cancel" },
      {
        text: "삭제",
        style: "destructive",
        onPress: () =>
          deletePost.mutate(Number(id), {
            onSuccess: () => router.back(),
            onError: () => Alert.alert("삭제 실패", "잠시 후 다시 시도해주세요."),
          }),
      },
    ]);

  if (query.isLoading) {
    return (
      <View style={[styles.skeletonWrap, { backgroundColor: colors.background }]}>
        <TableSkeleton rows={5} />
      </View>
    );
  }
  if (query.isError || !post) {
    return <ErrorState onRetry={() => query.refetch()} />;
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.metaRow}>
          {team ? <View style={[styles.dot, { backgroundColor: team.color }]} /> : null}
          <Text style={[styles.meta, { color: colors.subText }]}>
            {team?.name ?? post.teamCode}
          </Text>
          {post.mine ? (
            <Pressable onPress={onDelete} style={styles.deleteBtn} hitSlop={8}>
              <Text style={[styles.deleteText, { color: colors.subText }]}>삭제</Text>
            </Pressable>
          ) : null}
        </View>

        <Text style={[styles.title, { color: colors.text }]}>{post.title}</Text>
        <Text style={[styles.author, { color: colors.subText }]}>
          {post.authorNickname} · {relativeTime(post.createdAt)} · 조회 {post.viewCount}
        </Text>

        <View style={[styles.divider, { backgroundColor: colors.border }]} />

        <Text style={[styles.body, { color: colors.text }]}>{post.content}</Text>

        <View style={styles.likeRow}>
          <View style={[styles.likeBox, { borderColor: colors.border }]}>
            <Text style={[styles.likeText, { color: colors.text }]}>♥ {post.likeCount}</Text>
          </View>
        </View>

        {/* 댓글은 다음 단계에서 추가 */}
        <Text style={[styles.commentPlaceholder, { color: colors.subText }]}>
          댓글 {post.commentCount} · 댓글 기능은 곧 추가됩니다
        </Text>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: spacing.md, gap: spacing.sm },
  skeletonWrap: { flex: 1, padding: spacing.md },
  metaRow: { flexDirection: "row", alignItems: "center", gap: 6 },
  dot: { width: 7, height: 7, borderRadius: radius.pill },
  meta: { fontSize: 12 },
  deleteBtn: { marginLeft: "auto" },
  deleteText: { fontSize: 13 },
  title: { fontSize: 18, fontWeight: "700", lineHeight: 24 },
  author: { fontSize: 12 },
  divider: { height: border.hairline, marginVertical: spacing.sm },
  body: { fontSize: 15, lineHeight: 24 },
  likeRow: { alignItems: "center", marginVertical: spacing.lg },
  likeBox: { paddingHorizontal: spacing.xl, paddingVertical: spacing.sm + 2, borderRadius: radius.pill, borderWidth: border.card },
  likeText: { fontSize: 15, fontWeight: "600" },
  commentPlaceholder: { fontSize: 13, textAlign: "center", paddingVertical: spacing.lg },
});
