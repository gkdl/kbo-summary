import { StyleSheet, Text, View } from "react-native";

interface Props {
  recentForm: string[];
  size?: number;
}

const COLORS: Record<string, string> = {
  W: "#1F8A4E",
  L: "#C0392B",
  D: "#7F8C8D",
};

const SYMBOLS: Record<string, string> = {
  W: "●",
  L: "○",
  D: "△",
};

export function RecentFormDots({ recentForm, size = 14 }: Props) {
  return (
    <View style={styles.container}>
      {recentForm.map((result, index) => (
        <Text
          key={index}
          style={{
            fontSize: size,
            color: COLORS[result] ?? "#999999",
            marginRight: 3,
          }}
        >
          {SYMBOLS[result] ?? "·"}
        </Text>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flexDirection: "row", alignItems: "center" },
});
