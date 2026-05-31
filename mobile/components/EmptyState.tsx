import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

interface Props {
  message: string;
  icon?: string;
}

export function EmptyState({ message, icon = "📭" }: Props) {
  const { colors } = useTheme();
  return (
    <View style={styles.container}>
      <Text style={styles.icon}>{icon}</Text>
      <Text style={[styles.message, { color: colors.subText }]}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { padding: 32, alignItems: "center", justifyContent: "center", gap: 8 },
  icon: { fontSize: 32 },
  message: { fontSize: 14, textAlign: "center" },
});
