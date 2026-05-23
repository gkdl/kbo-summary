import { useEffect, useRef } from "react";
import { Animated, StyleSheet } from "react-native";
import { useTheme } from "@react-navigation/native";

interface Props {
  width?: number;
  height?: number;
  borderRadius?: number;
}

export function SkeletonLoader({ width = 200, height = 16, borderRadius = 4 }: Props) {
  const { colors } = useTheme();
  const opacity = useRef(new Animated.Value(0.3)).current;

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
