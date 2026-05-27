import { useEffect, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { apiDebug } from "../api/client";

/**
 * 화면 우상단에 떠다니는 진단 패널.
 * - API baseURL / 마지막 요청 URL / 마지막 에러 표시
 * - 탭하면 펼침/접힘
 *
 * 디버깅 끝나면 _layout.tsx 의 <DebugOverlay /> 와 이 파일을 제거.
 */
export function DebugOverlay() {
  const [open, setOpen] = useState(false);
  const [, force] = useState(0);

  // apiDebug 는 mutable 객체라 React 가 자동 리렌더하지 않으니
  // 0.5초마다 강제 리렌더로 최신값 반영
  useEffect(() => {
    const t = setInterval(() => force((n) => n + 1), 500);
    return () => clearInterval(t);
  }, []);

  return (
    <Pressable
      onPress={() => setOpen((o) => !o)}
      style={[styles.box, open ? styles.boxOpen : null]}
    >
      <Text style={styles.title}>DEBUG {open ? "▼" : "▶"}</Text>
      {open ? (
        <View style={styles.body}>
          <Text style={styles.line}>base: {apiDebug.baseURL}</Text>
          {apiDebug.lastUrl ? <Text style={styles.line}>req:  {apiDebug.lastUrl}</Text> : null}
          {apiDebug.lastStatus ? <Text style={styles.line}>status: {apiDebug.lastStatus}</Text> : null}
          {apiDebug.lastError ? (
            <Text style={[styles.line, styles.error]}>err:  {apiDebug.lastError}</Text>
          ) : null}
        </View>
      ) : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  box: {
    position: "absolute",
    top: 40,
    right: 8,
    backgroundColor: "rgba(0,0,0,0.75)",
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
    zIndex: 9999,
    maxWidth: 280,
  },
  boxOpen: { paddingVertical: 8 },
  title: { color: "#FF6B6B", fontSize: 10, fontWeight: "700" },
  body: { marginTop: 4, gap: 2 },
  line: { color: "#FFFFFF", fontSize: 10, fontFamily: "monospace" },
  error: { color: "#FFB4B4" },
});
