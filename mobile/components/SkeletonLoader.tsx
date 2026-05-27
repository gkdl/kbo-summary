import { useEffect, useState } from "react";
import { Animated, StyleSheet } from "react-native";
import { useTheme } from "../hooks/useTheme";

interface Props {
  width?: number;
  height?: number;
  borderRadius?: number;
}

export function SkeletonLoader({ width = 200, height = 16, borderRadius = 4 }: Props) {
  const { colors } = useTheme();
  // useState lazy initializer 로 Animated.Value 를 1회만 생성.
  // useRef 의 .current 를 render 시점에 접근하면 react-hooks/refs 규칙 위반.
  const [opacity] = useState(() => new Animated.Value(0.3));

  useEffect(() => {
    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(opacity, { toValue: 0.7, duration: 800, useNativeDriver: true }),
        Animated.timing(opacity, { toValue: 0.3, duration: 800, useNativeDriver: true }),
      ]),
    );
    loop.start();
    return () => loop.stop();
  }, [opacity]);

  return (
    <Animated.View
      style={[
        styles.base,
        { width, height, borderRadius, backgroundColor: colors.border, opacity },
      ]}
    />
  );
}

const styles = StyleSheet.create({
  base: {},
});
