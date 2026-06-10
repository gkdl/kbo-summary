import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

interface Props {
  message: string;
  icon?: string;
  hint?: string;
}

export function EmptyState({ message, icon = "📭", hint }: Props) {
  const { colors } = useTheme();
  return (
    <View style={styles.container}>
      <Text style={styles.icon}>{icon}</Text>
      <Text style={[styles.message, { color: colors.subText }]}>{message}</Text>
      {hint ? <Text style={[styles.hint, { color: colors.subText }]}>{hint}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { padding: 32, alignItems: "center", justifyContent: "center", gap: 8 },
  icon: { fontSize: 32 },
  message: { fontSize: 14, textAlign: "center" },
  hint: { fontSize: 12, textAlign: "center", opacity: 0.7 },
});
