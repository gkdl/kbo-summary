import { useColorScheme } from "react-native";

/**
 * 자체 useTheme — 기존 @react-navigation/native 의 동일 인터페이스 ({ colors: {...} }) 유지.
 * SDK 56 에서 expo-router 가 react-navigation 직접 의존을 비호환으로 막아 자체 구현으로 교체.
 *
 * 모든 호출 사이트가 `const { colors } = useTheme();` 형태라 hook 만 교체하면 코드 그대로 동작.
 */

export interface ThemeColors {
  primary: string;
  background: string;
  card: string;
  text: string;
  subText: string;
  border: string;
  notification: string;
}

export interface Theme {
  dark: boolean;
  colors: ThemeColors;
}

const lightColors: ThemeColors = {
  primary: "#3380FF",
  background: "#F2F2F7",
  card: "#FFFFFF",
  text: "#1C1C1E",
  subText: "#6C6C70",
  border: "#D8D8DC",
  notification: "#FF3B30",
};

// 피그마 디자인 기준 다크 테마
const darkColors: ThemeColors = {
  primary: "#3380FF",
  background: "#121217",
  card: "#1E1E29",
  text: "#FFFFFF",
  subText: "#9999A6",
  border: "#383847",
  notification: "#FF453A",
};

export function useTheme(): Theme {
  return {
    dark: true,
    colors: darkColors,
  };
}
