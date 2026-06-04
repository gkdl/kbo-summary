import { StyleSheet } from "react-native";

// 카드·버튼·뱃지 등에서 공유하는 시각 토큰.
// 동일 의미의 값은 한 곳에서 관리해 컴포넌트 간 일관성을 유지한다.

export const radius = {
  sm: 8,
  md: 12,
  lg: 16,
  pill: 999,
} as const;

export const border = {
  hairline: StyleSheet.hairlineWidth,
  card: 1,
  emphasis: 2,
} as const;

export const opacity = {
  pressed: 0.7,
  disabled: 0.4,
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
} as const;
