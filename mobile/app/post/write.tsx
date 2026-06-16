import { useState } from "react";
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Image,
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
import { TEAMS } from "../../constants/teams";
import { border, opacity, radius, spacing } from "../../constants/tokens";
import { useCreatePost } from "../../hooks/usePostMutations";
import { useTheme } from "../../hooks/useTheme";
import { imageFullUrl, pickImages, uploadImage } from "../../lib/imageUpload";

const MAX_IMAGES = 4;

export default function WritePostScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const params = useLocalSearchParams<{ team?: string }>();
  const createPost = useCreatePost();

  // 글을 쓰는 게시판(구단). 진입 시 받은 team, 없으면 첫 구단.
  const [team, setTeam] = useState<string>(params.team ?? TEAMS[0].code);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [images, setImages] = useState<string[]>([]); // 업로드된 서버 경로
  const [uploading, setUploading] = useState(false);

  const canSubmit =
    title.trim().length > 0 && content.trim().length > 0 && !createPost.isPending && !uploading;

  const onAddImages = async () => {
    const remaining = MAX_IMAGES - images.length;
    if (remaining <= 0) return;
    const uris = await pickImages(remaining);
    if (uris.length === 0) return;
    setUploading(true);
    try {
      const uploaded: string[] = [];
      for (const uri of uris.slice(0, remaining)) {
        uploaded.push(await uploadImage(uri));
      }
      setImages((prev) => [...prev, ...uploaded].slice(0, MAX_IMAGES));
    } catch {
      Alert.alert("업로드 실패", "이미지 업로드에 실패했어요. 다시 시도해주세요.");
    } finally {
      setUploading(false);
    }
  };

  const removeImage = (url: string) => setImages((prev) => prev.filter((u) => u !== url));

  const onSubmit = () => {
    if (!canSubmit) return;
    createPost.mutate(
      { teamCode: team, title: title.trim(), content: content.trim(), imageUrls: images },
      {
        onSuccess: (data) => {
          router.replace(data ? `/post/${data.postId}` : "/community");
        },
        onError: () => Alert.alert("등록 실패", "잠시 후 다시 시도해주세요."),
      },
    );
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      <View style={[styles.header, { borderBottomColor: colors.border }]}>
        <Pressable onPress={() => router.back()} hitSlop={8}>
          <Text style={[styles.cancel, { color: colors.subText }]}>취소</Text>
        </Pressable>
        <Text style={[styles.headerTitle, { color: colors.text }]}>글쓰기</Text>
        <View style={{ width: 32 }} />
      </View>

      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <ScrollView contentContainerStyle={styles.body} keyboardShouldPersistTaps="handled">
          <Text style={[styles.boardLabel, { color: colors.subText }]}>게시판</Text>
          <FlatList
            horizontal
            showsHorizontalScrollIndicator={false}
            data={TEAMS}
            keyExtractor={(t) => t.code}
            contentContainerStyle={{ gap: spacing.sm }}
            renderItem={({ item }) => {
              const active = team === item.code;
              return (
                <Pressable
                  onPress={() => setTeam(item.code)}
                  style={[
                    styles.chip,
                    {
                      backgroundColor: active ? item.color : colors.card,
                      borderColor: active ? item.color : colors.border,
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

          <TextInput
            value={title}
            onChangeText={setTitle}
            placeholder="제목을 입력하세요"
            placeholderTextColor={colors.subText}
            maxLength={100}
            style={[styles.titleInput, { color: colors.text, borderBottomColor: colors.border }]}
          />

          <TextInput
            value={content}
            onChangeText={setContent}
            placeholder="자유롭게 이야기를 나눠보세요. 서로 존중하는 댓글 문화를 지켜주세요 :)"
            placeholderTextColor={colors.subText}
            multiline
            maxLength={2000}
            style={[styles.contentInput, { color: colors.text }]}
          />

          {/* 이미지 첨부 (최대 4장) */}
          <View style={styles.imageRow}>
            {images.map((url) => (
              <View key={url} style={styles.thumbWrap}>
                <Image source={{ uri: imageFullUrl(url) }} style={styles.thumb} />
                <Pressable onPress={() => removeImage(url)} style={styles.thumbRemove} hitSlop={6}>
                  <Text style={styles.thumbRemoveText}>×</Text>
                </Pressable>
              </View>
            ))}
            {images.length < MAX_IMAGES ? (
              <Pressable
                onPress={onAddImages}
                disabled={uploading}
                style={[styles.addImage, { borderColor: colors.border }]}
              >
                {uploading ? (
                  <ActivityIndicator color={colors.subText} />
                ) : (
                  <Text style={[styles.addImageText, { color: colors.subText }]}>
                    📷{"\n"}{images.length}/{MAX_IMAGES}
                  </Text>
                )}
              </Pressable>
            ) : null}
          </View>
        </ScrollView>

        {/* 등록 — 우측 하단 FAB */}
        <Pressable
          onPress={onSubmit}
          disabled={!canSubmit}
          style={({ pressed }) => [
            styles.fab,
            { backgroundColor: colors.primary, opacity: !canSubmit ? opacity.disabled : pressed ? opacity.pressed : 1 },
          ]}
        >
          <Text style={styles.fabText}>{createPost.isPending ? "등록 중" : "등록"}</Text>
        </Pressable>
      </KeyboardAvoidingView>

      <AdBanner />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
    borderBottomWidth: border.hairline,
  },
  cancel: { fontSize: 15 },
  headerTitle: { fontSize: 16, fontWeight: "700" },
  fab: {
    position: "absolute",
    right: spacing.lg,
    bottom: spacing.lg,
    paddingHorizontal: spacing.xl,
    height: 48,
    borderRadius: radius.pill,
    alignItems: "center",
    justifyContent: "center",
  },
  fabText: { color: "#FFFFFF", fontSize: 15, fontWeight: "700" },
  body: { padding: spacing.md, gap: spacing.md, paddingBottom: 90 },
  boardLabel: { fontSize: 12, fontWeight: "600" },
  chip: { paddingHorizontal: spacing.md, paddingVertical: 7, borderRadius: radius.pill, borderWidth: border.card },
  chipText: { fontSize: 13, fontWeight: "600" },
  titleInput: { fontSize: 17, paddingVertical: spacing.sm, borderBottomWidth: border.hairline },
  contentInput: { fontSize: 15, lineHeight: 24, minHeight: 180, textAlignVertical: "top" },
  imageRow: { flexDirection: "row", flexWrap: "wrap", gap: spacing.sm },
  thumbWrap: { width: 72, height: 72 },
  thumb: { width: 72, height: 72, borderRadius: radius.md },
  thumbRemove: {
    position: "absolute",
    top: -6,
    right: -6,
    width: 22,
    height: 22,
    borderRadius: radius.pill,
    backgroundColor: "rgba(0,0,0,0.7)",
    alignItems: "center",
    justifyContent: "center",
  },
  thumbRemoveText: { color: "#FFFFFF", fontSize: 15, fontWeight: "700", lineHeight: 17 },
  addImage: {
    width: 72,
    height: 72,
    borderRadius: radius.md,
    borderWidth: border.card,
    borderStyle: "dashed",
    alignItems: "center",
    justifyContent: "center",
  },
  addImageText: { fontSize: 12, textAlign: "center", lineHeight: 18 },
});
