import { Image, Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";

import { ChatIcon } from "./icons/TabIcons";
import { border, opacity, radius, spacing } from "../constants/tokens";
import { useTheme } from "../hooks/useTheme";
import { imageFullUrl } from "../lib/imageUpload";
import type { PostListItem } from "../types/community";
import { relativeTime } from "../utils/relativeTime";

interface Props {
  post: PostListItem;
}

const HOT_THRESHOLD = 10;
const LIKE_COLOR = "#FF5A7A";

export function PostCard({ post }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const hot = post.likeCount >= HOT_THRESHOLD;

  return (
    <Pressable
      onPress={() => router.push(`/post/${post.postId}`)}
      style={({ pressed }) => [styles.card, pressed && { opacity: opacity.pressed }]}
    >
      <View style={styles.body}>
        <View style={styles.textCol}>
          <View style={styles.titleRow}>
            {hot ? (
              <View style={[styles.hotChip, { backgroundColor: colors.primary }]}>
                <Text style={styles.hotChipText}>인기</Text>
              </View>
            ) : null}
            <Text style={[styles.title, { color: colors.text }]} numberOfLines={2}>
              {post.title}
            </Text>
          </View>

          <View style={styles.metaRow}>
            <Text style={[styles.author, { color: colors.subText }]} numberOfLines={1}>
              {post.authorNickname} · {relativeTime(post.createdAt)}
            </Text>
            <View style={styles.counts}>
              <Text style={[styles.likeText, { color: LIKE_COLOR }]}>♥ {post.likeCount}</Text>
              <View style={styles.commentItem}>
                <ChatIcon color={colors.subText} size={13} />
                <Text style={[styles.countText, { color: colors.subText }]}>{post.commentCount}</Text>
              </View>
            </View>
          </View>
        </View>

        {post.thumbnailUrl ? (
          <Image
            source={{ uri: imageFullUrl(post.thumbnailUrl) }}
            style={[styles.thumb, { backgroundColor: colors.card }]}
            resizeMode="cover"
          />
        ) : null}
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { paddingVertical: spacing.md },
  body: { flexDirection: "row", gap: spacing.md },
  textCol: { flex: 1, gap: 8 },
  titleRow: { flexDirection: "row", alignItems: "flex-start", gap: 6 },
  hotChip: { paddingHorizontal: 6, paddingVertical: 2, borderRadius: 4, marginTop: 1 },
  hotChipText: { fontSize: 10, fontWeight: "700", color: "#FFFFFF" },
  title: { flex: 1, fontSize: 15, fontWeight: "600", lineHeight: 21 },
  metaRow: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: spacing.sm },
  author: { flex: 1, fontSize: 12 },
  counts: { flexDirection: "row", alignItems: "center", gap: spacing.md },
  likeText: { fontSize: 12, fontWeight: "600" },
  commentItem: { flexDirection: "row", alignItems: "center", gap: 3 },
  countText: { fontSize: 12 },
  thumb: { width: 60, height: 60, borderRadius: radius.md },
});

// 카드 사이 구분선 (FlatList ItemSeparator 용)
export function PostSeparator() {
  const { colors } = useTheme();
  return <View style={{ height: border.hairline, backgroundColor: colors.border }} />;
}
