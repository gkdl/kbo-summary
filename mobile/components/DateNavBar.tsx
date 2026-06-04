import { useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import DateTimePickerModal from "react-native-modal-datetime-picker";

import { useTheme } from "../hooks/useTheme";
import { opacity, spacing } from "../constants/tokens";
import { displayDate, parseYyyymmdd, shiftDays, toYyyymmdd } from "../utils/date";
import { CalendarIcon, ChevronLeftIcon, ChevronRightIcon } from "./icons/NavIcons";

interface Props {
  value: string;
  onChange: (yyyymmdd: string) => void;
  // 오늘로 점프하는 long-press 동작의 기준 (홈=오늘, 하이라이트=어제)
  defaultDate?: () => string;
  // 캘린더 모달 최대 선택일 (예: 하이라이트는 오늘까지만)
  maxDate?: Date;
}

export function DateNavBar({ value, onChange, defaultDate, maxDate }: Props) {
  const { colors } = useTheme();
  const [pickerVisible, setPickerVisible] = useState(false);

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
        onPress={() => setPickerVisible(true)}
        onLongPress={() => defaultDate && onChange(defaultDate())}
        style={({ pressed }) => [styles.dateButton, pressed && { opacity: opacity.pressed }]}
      >
        <Text style={[styles.dateText, { color: colors.text }]}>{displayDate(value)}</Text>
        <CalendarIcon color={colors.subText} size={14} />
      </Pressable>

      <Pressable
        accessibilityLabel="다음 날짜"
        hitSlop={8}
        onPress={() => onChange(shiftDays(value, 1))}
        style={({ pressed }) => [styles.iconButton, pressed && { opacity: opacity.pressed }]}
      >
        <ChevronRightIcon color={colors.primary} size={20} />
      </Pressable>

      <DateTimePickerModal
        isVisible={pickerVisible}
        mode="date"
        date={parseYyyymmdd(value)}
        minimumDate={new Date(new Date().getFullYear() - 1, 0, 1)}
        maximumDate={maxDate ?? new Date(new Date().getFullYear() + 1, 11, 31)}
        locale="ko-KR"
        confirmTextIOS="선택"
        cancelTextIOS="취소"
        onConfirm={(date) => {
          onChange(toYyyymmdd(date));
          setPickerVisible(false);
        }}
        onCancel={() => setPickerVisible(false)}
      />
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
  // 44×44 touch target 확보
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
  },
  dateText: { fontSize: 15, fontWeight: "700" },
});
