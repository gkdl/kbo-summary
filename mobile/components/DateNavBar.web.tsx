// Web 빌드 전용 — @react-native-community/datetimepicker 가 웹을 지원하지 않아
// HTML <input type="date"> 를 투명하게 오버레이 해 브라우저 기본 날짜 피커를 띄운다.
// Metro 가 .web.tsx 를 자동 선택하므로 native 모듈 import 가 발생하지 않는다.
import { Pressable, StyleSheet, Text, View } from "react-native";

import { useTheme } from "../hooks/useTheme";
import { opacity, spacing } from "../constants/tokens";
import { displayDate, shiftDays } from "../utils/date";
import { CalendarIcon, ChevronLeftIcon, ChevronRightIcon } from "./icons/NavIcons";

interface Props {
  value: string;
  onChange: (yyyymmdd: string) => void;
  defaultDate?: () => string;
  maxDate?: Date;
}

// yyyymmdd → "YYYY-MM-DD" (HTML date input value 포맷)
function toIsoDate(yyyymmdd: string): string {
  return `${yyyymmdd.slice(0, 4)}-${yyyymmdd.slice(4, 6)}-${yyyymmdd.slice(6, 8)}`;
}

// "YYYY-MM-DD" → yyyymmdd
function fromIsoDate(iso: string): string {
  return iso.replace(/-/g, "");
}

function pad(n: number): string {
  return String(n).padStart(2, "0");
}

function toIsoFromDate(d: Date): string {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

export function DateNavBar({ value, onChange, defaultDate, maxDate }: Props) {
  const { colors } = useTheme();
  const minIso = `${new Date().getFullYear() - 1}-01-01`;
  const maxIso = maxDate
    ? toIsoFromDate(maxDate)
    : `${new Date().getFullYear() + 1}-12-31`;

  return (
    <View style={[styles.bar, { borderBottomColor: colors.border }]}>
      <Pressable
        accessibilityLabel="이전 날짜"
        hitSlop={8}
        onPress={() => onChange(shiftDays(value, -1))}
        style={({ pressed }) => [styles.iconButton, pressed && { opacity: opacity.pressed }]}
      >
        <ChevronLeftIcon color={colors.primary} size={20} />
      </Pressable>

      <Pressable
        accessibilityLabel="날짜 선택"
        onLongPress={() => defaultDate && onChange(defaultDate())}
        style={styles.dateButton}
      >
        <Text style={[styles.dateText, { color: colors.text }]}>{displayDate(value)}</Text>
        <CalendarIcon color={colors.subText} size={14} />
        {/* 투명 HTML 날짜 input 오버레이 — 클릭 시 브라우저 기본 피커가 뜸 */}
        {/* @ts-expect-error react-native-web 에서 native input 사용 */}
        <input
          type="date"
          value={toIsoDate(value)}
          min={minIso}
          max={maxIso}
          onChange={(e: { target: { value: string } }) => {
            if (e.target.value) onChange(fromIsoDate(e.target.value));
          }}
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            width: "100%",
            height: "100%",
            opacity: 0,
            cursor: "pointer",
            border: "none",
            background: "transparent",
          }}
        />
      </Pressable>

      <Pressable
        accessibilityLabel="다음 날짜"
        hitSlop={8}
        onPress={() => onChange(shiftDays(value, 1))}
        style={({ pressed }) => [styles.iconButton, pressed && { opacity: opacity.pressed }]}
      >
        <ChevronRightIcon color={colors.primary} size={20} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  bar: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  iconButton: {
    width: 44,
    height: 44,
    alignItems: "center",
    justifyContent: "center",
  },
  dateButton: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.xs + 2,
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.md,
    position: "relative",
  },
  dateText: { fontSize: 15, fontWeight: "700" },
});
