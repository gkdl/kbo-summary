import { useState } from "react";
import {
  Alert,
  FlatList,
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

import { TEAMS } from "../../constants/teams";
import { border, opacity, radius, spacing } from "../../constants/tokens";
import { useCreatePost } from "../../hooks/usePostMutations";
import { useTheme } from "../../hooks/useTheme";

export default function WritePostScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const params = useLocalSearchParams<{ team?: string }>();
  const createPost = useCreatePost();

  // 글을 쓰는 게시판(구단). 진입 시 받은 team, 없으면 첫 구단.
  const [team, setTeam] = useState<string>(params.team ?? TEAMS[0].code);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  const canSubmit = title.trim().length > 0 && content.trim().length > 0 && !createPost.isPending;

  const onSubmit = () => {
    if (!canSubmit) return;
    createPost.mutate(
      { teamCode: team, title: title.trim(), content: content.trim() },
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
        <Pressable
          onPress={onSubmit}
          disabled={!canSubmit}
          style={({ pressed }) => [
            styles.submit,
            { backgroundColor: colors.primary, opacity: !canSubmit ? opacity.disabled : pressed ? opacity.pressed : 1 },
          ]}
        >
          <Text style={styles.submitText}>{createPost.isPending ? "등록 중" : "등록"}</Text>
        </Pressable>
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
        </ScrollView>
      </KeyboardAvoidingView>
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
  submit: { paddingHorizontal: spacing.md, paddingVertical: 7, borderRadius: radius.md },
  submitText: { color: "#FFFFFF", fontSize: 15, fontWeight: "600" },
  body: { padding: spacing.md, gap: spacing.md },
  boardLabel: { fontSize: 12, fontWeight: "600" },
  chip: { paddingHorizontal: spacing.md, paddingVertical: 7, borderRadius: radius.pill, borderWidth: border.card },
  chipText: { fontSize: 13, fontWeight: "600" },
  titleInput: { fontSize: 17, paddingVertical: spacing.sm, borderBottomWidth: border.hairline },
  contentInput: { fontSize: 15, lineHeight: 24, minHeight: 220, textAlignVertical: "top" },
});
