import { useColorScheme } from "react-native";

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

const darkColors: ThemeColors = {
  primary: "#3380FF",
  background: "#121217",
  card: "#1E1E29",
  text: "#FFFFFF",
  subText: "#9999A6",
  border: "#383847",
  notification: "#FF453A",
};

// 라이트 팔레트 — 다크와 동일 시각 위계(text > subText > border)를 유지하면서
// 흰 배경에서도 primary 가 AA 대비를 확보하도록 약간 어둡게 보정
const lightColors: ThemeColors = {
  primary: "#2A6FD9",
  background: "#FFFFFF",
  card: "#F4F4F8",
  text: "#1A1A1F",
  subText: "#6E6E78",
  border: "#E0E0E6",
  notification: "#E63946",
};

export function useTheme(): Theme {
  const scheme = useColorScheme();
  const dark = scheme !== "light";
  return {
    dark,
    colors: dark ? darkColors : lightColors,
  };
}
