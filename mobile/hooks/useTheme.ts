
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

export function useTheme(): Theme {
  return {
    dark: true,
    colors: darkColors,
  };
}
