import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";

import { getTeam } from "../constants/teams";
import { border, opacity, radius, spacing } from "../constants/tokens";
import { useTheme } from "../hooks/useTheme";
import type { PostListItem } from "../types/community";
import { relativeTime } from "../utils/relativeTime";

interface Props {
  post: PostListItem;
}

export function PostCard({ post }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const team = post.teamCode ? getTeam(post.teamCode) : undefined;

  return (
    <Pressable
      onPress={() => router.push(`/post/${post.postId}`)}
      style={({ pressed }) => [styles.card, pressed && { opacity: opacity.pressed }]}
    >
      <View style={styles.metaRow}>
        {team ? <View style={[styles.dot, { backgroundColor: team.color }]} /> : null}
        <Text style={[styles.meta, { color: colors.subText }]}>
          {team?.name ?? post.teamCode}
        </Text>
      </View>

      <Text style={[styles.title, { color: colors.text }]} numberOfLines={2}>
        {post.title}
      </Text>

      <View style={styles.footer}>
        <Text style={[styles.footerText, { color: colors.subText }]} numberOfLines={1}>
          {post.authorNickname} · {relativeTime(post.createdAt)}
        </Text>
        <View style={styles.counts}>
          <Text style={[styles.footerText, { color: colors.subText }]}>조회 {post.viewCount}</Text>
          <Text style={[styles.footerText, { color: colors.subText }]}>댓글 {post.commentCount}</Text>
          <Text style={[styles.footerText, { color: colors.subText }]}>♥ {post.likeCount}</Text>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { paddingVertical: spacing.md, gap: 6 },
  metaRow: { flexDirection: "row", alignItems: "center", gap: 6 },
  dot: { width: 7, height: 7, borderRadius: radius.pill },
  meta: { fontSize: 12 },
  title: { fontSize: 15, fontWeight: "600", lineHeight: 20 },
  footer: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginTop: 2 },
  footerText: { fontSize: 12 },
  counts: { flexDirection: "row", gap: spacing.sm + 2 },
});

// 카드 사이 구분선 (FlatList ItemSeparator 용)
export function PostSeparator() {
  const { colors } = useTheme();
  return <View style={{ height: border.hairline, backgroundColor: colors.border }} />;
}
