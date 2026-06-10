import { Pressable, StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

interface Props {
  message?: string;
  onRetry?: () => void;
}

export function ErrorState({ message = "잠시 후 다시 시도해주세요", onRetry }: Props) {
  const { colors } = useTheme();
  return (
    <View style={styles.container}>
      <Text style={styles.icon}>⚾</Text>
      <Text style={[styles.message, { color: colors.text }]}>{message}</Text>
      {onRetry ? (
        <Pressable
          onPress={onRetry}
          style={({ pressed }) => [
            styles.button,
            { borderColor: colors.primary, opacity: pressed ? 0.6 : 1 },
          ]}
        >
          <Text style={[styles.buttonLabel, { color: colors.primary }]}>다시 시도</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { padding: 32, alignItems: "center", justifyContent: "center", gap: 12 },
  icon: { fontSize: 32 },
  message: { fontSize: 14, textAlign: "center" },
  button: { paddingHorizontal: 16, paddingVertical: 8, borderRadius: 6, borderWidth: 1 },
  buttonLabel: { fontSize: 14, fontWeight: "600" },
});
