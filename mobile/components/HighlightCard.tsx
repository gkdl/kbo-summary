import { Alert, Image, Linking, Pressable, StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import type { Highlight } from "../types/game";

interface Props {
  highlight: Highlight;
}

/**
 * KBO 경기 하이라이트 카드 — 썸네일 + 재생 버튼.
 * 탭하면 외부 YouTube 앱(또는 브라우저)으로 열린다.
 * 썸네일은 YouTube 가 제공하는 표준 hqdefault.jpg 사용 (별도 호스팅 불필요, 항상 존재).
 */
export function HighlightCard({ highlight }: Props) {
  const { colors } = useTheme();
  const thumbUrl = `https://i.ytimg.com/vi/${highlight.youtubeVideoId}/hqdefault.jpg`;
  // YouTube 앱 deeplink → 설치 안 됐으면 자동으로 웹 브라우저로 fallback
  const videoUrl = `https://www.youtube.com/watch?v=${highlight.youtubeVideoId}`;

  const openVideo = async () => {
    try {
      await Linking.openURL(videoUrl);
    } catch {
      Alert.alert("영상을 열 수 없습니다", videoUrl);
    }
  };

  return (
    <Pressable
      onPress={openVideo}
      style={({ pressed }) => [
        styles.card,
        { backgroundColor: colors.card, borderColor: colors.border, opacity: pressed ? 0.85 : 1 },
      ]}
    >
      <View style={styles.thumbWrap}>
        <Image source={{ uri: thumbUrl }} style={styles.thumb} resizeMode="cover" />
      </View>
      <View style={styles.captionRow}>
        <Text style={[styles.label, { color: colors.text }]}>경기 하이라이트</Text>
        {highlight.title ? (
          <Text style={[styles.subtitle, { color: colors.subText }]} numberOfLines={1}>
            {highlight.title}
          </Text>
        ) : null}
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { borderRadius: 10, borderWidth: 1, overflow: "hidden" },
  thumbWrap: { width: "100%", aspectRatio: 16 / 9, backgroundColor: "#000000" },
  thumb: { width: "100%", height: "100%" },
  captionRow: { paddingHorizontal: 12, paddingVertical: 10, gap: 2 },
  label: { fontSize: 14, fontWeight: "700" },
  subtitle: { fontSize: 12 },
});
