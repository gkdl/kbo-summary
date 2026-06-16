import { Modal, Pressable, StyleSheet, Text, View } from "react-native";

import { border, opacity, radius, spacing } from "../constants/tokens";
import { useTheme } from "../hooks/useTheme";
import { REPORT_REASONS } from "../hooks/useModeration";

interface Props {
  visible: boolean;
  targetLabel: string; // "게시글" | "댓글"
  onReport: (reason: string) => void;
  onBlock: () => void;
  onClose: () => void;
}

/**
 * 신고 사유 선택 + 작성자 차단 바텀시트. 신고 사유가 4개라 Alert 로는 부족해 Modal 로 구성.
 */
export function ModerationSheet({ visible, targetLabel, onReport, onBlock, onClose }: Props) {
  const { colors } = useTheme();

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={styles.backdrop} onPress={onClose}>
        <Pressable style={[styles.sheet, { backgroundColor: colors.card }]} onPress={() => {}}>
          <Text style={[styles.title, { color: colors.subText }]}>{targetLabel} 신고 사유</Text>

          {REPORT_REASONS.map((r) => (
            <Pressable
              key={r.key}
              onPress={() => onReport(r.key)}
              style={({ pressed }) => [styles.row, pressed && { opacity: opacity.pressed }]}
            >
              <Text style={[styles.rowText, { color: colors.text }]}>{r.label}</Text>
            </Pressable>
          ))}

          <View style={[styles.divider, { backgroundColor: colors.border }]} />

          <Pressable
            onPress={onBlock}
            style={({ pressed }) => [styles.row, pressed && { opacity: opacity.pressed }]}
          >
            <Text style={[styles.rowText, { color: colors.notification }]}>작성자 차단</Text>
          </Pressable>

          <Pressable
            onPress={onClose}
            style={({ pressed }) => [styles.cancel, { borderColor: colors.border }, pressed && { opacity: opacity.pressed }]}
          >
            <Text style={[styles.rowText, { color: colors.subText }]}>취소</Text>
          </Pressable>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: { flex: 1, backgroundColor: "rgba(0,0,0,0.45)", justifyContent: "flex-end" },
  sheet: {
    borderTopLeftRadius: radius.lg,
    borderTopRightRadius: radius.lg,
    padding: spacing.md,
    paddingBottom: spacing.xl,
    gap: 2,
  },
  title: { fontSize: 12, fontWeight: "600", paddingVertical: spacing.sm, paddingHorizontal: spacing.sm },
  row: { paddingVertical: 14, paddingHorizontal: spacing.sm },
  rowText: { fontSize: 15, fontWeight: "500" },
  divider: { height: border.hairline, marginVertical: spacing.xs },
  cancel: {
    marginTop: spacing.sm,
    paddingVertical: 14,
    alignItems: "center",
    borderRadius: radius.md,
    borderWidth: border.card,
  },
});
