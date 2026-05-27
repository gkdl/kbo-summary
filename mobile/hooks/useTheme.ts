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
  border: string;
  notification: string;
}

export interface Theme {
  dark: boolean;
  colors: ThemeColors;
}

// react-navigation 의 DefaultTheme / DarkTheme 색상을 그대로 옮겼다 (시각적 변화 없음)
const lightColors: ThemeColors = {
  primary: "rgb(0, 122, 255)",
  background: "rgb(242, 242, 242)",
  card: "rgb(255, 255, 255)",
  text: "rgb(28, 28, 30)",
  border: "rgb(216, 216, 216)",
  notification: "rgb(255, 59, 48)",
};

const darkColors: ThemeColors = {
  primary: "rgb(10, 132, 255)",
  background: "rgb(0, 0, 0)",
  card: "rgb(18, 18, 18)",
  text: "rgb(229, 229, 231)",
  border: "rgb(39, 39, 41)",
  notification: "rgb(255, 69, 58)",
};

export function useTheme(): Theme {
  const scheme = useColorScheme();
  const dark = scheme === "dark";
  return {
    dark,
    colors: dark ? darkColors : lightColors,
  };
}
