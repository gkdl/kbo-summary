import { Pressable, StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import { TEAMS } from "../constants/teams";

interface Props {
  selectedCode?: string | null;
  onSelect: (teamCode: string) => void;
}

export function TeamSelector({ selectedCode, onSelect }: Props) {
  const { colors } = useTheme();

  return (
    <View style={styles.grid}>
      {TEAMS.map((team) => {
        const selected = team.code === selectedCode;
        return (
          <Pressable
            key={team.code}
            onPress={() => onSelect(team.code)}
            style={({ pressed }) => [
              styles.chip,
              {
                backgroundColor: selected ? team.color : colors.card,
                borderColor: selected ? team.color : colors.border,
                opacity: pressed ? 0.7 : 1,
              },
            ]}
          >
            <Text
              style={[
                styles.label,
                { color: selected ? "#FFFFFF" : colors.text },
              ]}
            >
              {team.shortName}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  grid: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  chip: {
    minWidth: 64,
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  label: { fontSize: 14, fontWeight: "600" },
});
