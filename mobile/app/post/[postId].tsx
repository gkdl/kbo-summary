import { useState } from "react";
import {
  Alert,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";

import { AdBanner } from "../../components/AdBanner";
import { ErrorState } from "../../components/ErrorState";
import { TableSkeleton } from "../../components/skeletons/TableSkeleton";
import { getTeam } from "../../constants/teams";
import { border, opacity, radius, spacing } from "../../constants/tokens";
import { useAuth } from "../../hooks/useAuth";
import { useComments, useCreateComment, useDeleteComment, useToggleLike } from "../../hooks/useComments";
import { usePost } from "../../hooks/usePosts";
import { useDeletePost } from "../../hooks/usePostMutations";
import { useTheme } from "../../hooks/useTheme";
import type { Comment } from "../../types/comment";
import { relativeTime } from "../../utils/relativeTime";

export default function PostDetailScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { isAuthed } = useAuth();
  const { postId } = useLocalSearchParams<{ postId: string }>();
  const id = postId ?? "";

  const query = usePost(id);
  const commentsQuery = useComments(id);
  const deletePost = useDeletePost();
  const toggleLike = useToggleLike(id);
  const createComment = useCreateComment(id);
  const deleteComment = useDeleteComment(id);

  const [text, setText] = useState("");
  const [replyTo, setReplyTo] = useState<{ id: number; nickname: string } | null>(null);

  const post = query.data;
  const team = post?.teamCode ? getTeam(post.teamCode) : undefined;
  const comments = commentsQuery.data ?? [];

  const requireAuth = (): boolean => {
    if (!isAuthed) {
      router.push("/login");
      return false;
    }
    return true;
  };

  const onLike = () => {
    if (!requireAuth()) return;
    toggleLike.mutate();
  };

  const onDeletePost = () =>
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

  const onSubmitComment = () => {
    if (!requireAuth()) return;
    const content = text.trim();
    if (content.length === 0) return;
    createComment.mutate(
      { content, parentId: replyTo?.id ?? null },
      {
        onSuccess: () => {
          setText("");
          setReplyTo(null);
        },
        onError: () => Alert.alert("등록 실패", "잠시 후 다시 시도해주세요."),
      },
    );
  };

  const onDeleteComment = (commentId: number) =>
    Alert.alert("댓글 삭제", "이 댓글을 삭제할까요?", [
      { text: "취소", style: "cancel" },
      { text: "삭제", style: "destructive", onPress: () => deleteComment.mutate(commentId) },
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
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
          <View style={styles.metaRow}>
            {team ? <View style={[styles.dot, { backgroundColor: team.color }]} /> : null}
            <Text style={[styles.meta, { color: colors.subText }]}>{team?.name ?? post.teamCode}</Text>
            {post.mine ? (
              <Pressable onPress={onDeletePost} style={styles.deleteBtn} hitSlop={8}>
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
            <Pressable
              onPress={onLike}
              style={({ pressed }) => [
                styles.likeBox,
                {
                  borderColor: post.liked ? colors.primary : colors.border,
                  backgroundColor: post.liked ? colors.card : "transparent",
                  opacity: pressed ? opacity.pressed : 1,
                },
              ]}
            >
              <Text style={[styles.likeText, { color: post.liked ? colors.primary : colors.subText }]}>
                {post.liked ? "♥" : "♡"} {post.likeCount}
              </Text>
            </Pressable>
          </View>

          <View style={[styles.divider, { backgroundColor: colors.border }]} />
          <Text style={[styles.commentHeader, { color: colors.text }]}>댓글 {post.commentCount}</Text>

          {comments.length === 0 ? (
            <Text style={[styles.empty, { color: colors.subText }]}>첫 댓글을 남겨보세요</Text>
          ) : (
            comments.map((c) => (
              <CommentItem
                key={c.commentId}
                comment={c}
                colors={colors}
                onReply={(cm) => setReplyTo({ id: cm.commentId, nickname: cm.authorNickname })}
                onDelete={onDeleteComment}
              />
            ))
          )}
        </ScrollView>

        <View style={[styles.inputBar, { borderTopColor: colors.border, backgroundColor: colors.background }]}>
          {replyTo ? (
            <View style={styles.replyBanner}>
              <Text style={[styles.replyText, { color: colors.subText }]} numberOfLines={1}>
                {replyTo.nickname}님에게 답글
              </Text>
              <Pressable onPress={() => setReplyTo(null)} hitSlop={8}>
                <Text style={[styles.replyCancel, { color: colors.subText }]}>취소</Text>
              </Pressable>
            </View>
          ) : null}
          <View style={styles.inputRow}>
            <TextInput
              value={text}
              onChangeText={setText}
              placeholder={replyTo ? "답글을 입력하세요" : "댓글을 입력하세요"}
              placeholderTextColor={colors.subText}
              multiline
              maxLength={1000}
              style={[styles.input, { backgroundColor: colors.card, color: colors.text }]}
            />
            <Pressable
              onPress={onSubmitComment}
              disabled={text.trim().length === 0 || createComment.isPending}
              style={({ pressed }) => [
                styles.sendBtn,
                {
                  backgroundColor: colors.primary,
                  opacity: text.trim().length === 0 ? opacity.disabled : pressed ? opacity.pressed : 1,
                },
              ]}
            >
              <Text style={styles.sendText}>등록</Text>
            </Pressable>
          </View>
        </View>
      </KeyboardAvoidingView>

      <AdBanner />
    </SafeAreaView>
  );
}

interface ItemProps {
  comment: Comment;
  colors: { text: string; subText: string; border: string; primary: string; card: string };
  onReply: (c: Comment) => void;
  onDelete: (commentId: number) => void;
  isReply?: boolean;
}

function CommentItem({ comment, colors, onReply, onDelete, isReply }: ItemProps) {
  return (
    <View style={[styles.comment, isReply && styles.replyIndent]}>
      <View style={styles.commentHead}>
        <Text style={[styles.commentNick, { color: colors.text }]}>
          {comment.deleted ? "(삭제됨)" : comment.authorNickname}
        </Text>
      </View>
      <Text style={[styles.commentBody, { color: comment.deleted ? colors.subText : colors.text }]}>
        {comment.content}
      </Text>
      {!comment.deleted ? (
        <View style={styles.commentActions}>
          <Text style={[styles.commentTime, { color: colors.subText }]}>
            {relativeTime(comment.createdAt)}
          </Text>
          {!isReply ? (
            <Pressable onPress={() => onReply(comment)} hitSlop={6}>
              <Text style={[styles.commentAction, { color: colors.subText }]}>답글</Text>
            </Pressable>
          ) : null}
          {comment.mine ? (
            <Pressable onPress={() => onDelete(comment.commentId)} hitSlop={6}>
              <Text style={[styles.commentAction, { color: colors.subText }]}>삭제</Text>
            </Pressable>
          ) : null}
        </View>
      ) : null}

      {comment.replies.map((r) => (
        <CommentItem
          key={r.commentId}
          comment={r}
          colors={colors}
          onReply={onReply}
          onDelete={onDelete}
          isReply
        />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: spacing.md, gap: spacing.sm, paddingBottom: spacing.xl },
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
  likeRow: { alignItems: "flex-start", marginVertical: spacing.md },
  likeBox: { flexDirection: "row", alignItems: "center", paddingHorizontal: spacing.lg, paddingVertical: spacing.sm, borderRadius: radius.pill, borderWidth: border.card },
  likeText: { fontSize: 15, fontWeight: "600" },
  commentHeader: { fontSize: 14, fontWeight: "700" },
  empty: { fontSize: 13, textAlign: "center", paddingVertical: spacing.lg },
  comment: { paddingVertical: spacing.sm, gap: 4 },
  replyIndent: { marginLeft: spacing.lg, paddingLeft: spacing.sm },
  commentHead: { flexDirection: "row", alignItems: "center", gap: 6 },
  commentNick: { fontSize: 13, fontWeight: "600" },
  commentBody: { fontSize: 14, lineHeight: 20 },
  commentActions: { flexDirection: "row", gap: spacing.md, alignItems: "center" },
  commentTime: { fontSize: 12 },
  commentAction: { fontSize: 12, fontWeight: "500" },
  inputBar: { borderTopWidth: border.hairline, paddingHorizontal: spacing.md, paddingVertical: spacing.sm },
  replyBanner: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", paddingBottom: spacing.sm },
  replyText: { fontSize: 12, flex: 1 },
  replyCancel: { fontSize: 12 },
  inputRow: { flexDirection: "row", alignItems: "flex-end", gap: spacing.sm },
  input: { flex: 1, minHeight: 38, maxHeight: 100, borderRadius: radius.md, paddingHorizontal: spacing.md, paddingVertical: spacing.sm, fontSize: 14 },
  sendBtn: { paddingHorizontal: spacing.md, height: 38, borderRadius: radius.md, alignItems: "center", justifyContent: "center" },
  sendText: { color: "#FFFFFF", fontSize: 14, fontWeight: "600" },
});
