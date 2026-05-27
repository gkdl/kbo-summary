import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";
import { useTheme } from "../hooks/useTheme";

import type { TeamRoster } from "../types/team";

interface Props {
  roster: TeamRoster;
}

export function TeamRosterList({ roster }: Props) {
  const { colors } = useTheme();
  const router = useRouter();

  if (roster.players.length === 0) {
    return (
      <View style={styles.empty}>
        <Text style={{ color: colors.text, opacity: 0.6 }}>등록된 선수가 없습니다</Text>
      </View>
    );
  }

  return (
    <View style={[styles.list, { borderColor: colors.border, backgroundColor: colors.card }]}>
      {roster.players.map((player, index) => (
        <Pressable
          key={player.playerId}
          onPress={() => router.push(`/player/${player.playerId}`)}
          style={({ pressed }) => [
            styles.row,
            {
              borderTopColor: colors.border,
              borderTopWidth: index === 0 ? 0 : StyleSheet.hairlineWidth,
              opacity: pressed ? 0.6 : 1,
            },
          ]}
        >
          <Text style={[styles.number, { color: colors.text, opacity: 0.6 }]}>
            {player.backNumber ?? "-"}
          </Text>
          <Text style={[styles.name, { color: colors.text }]}>{player.name}</Text>
          {player.position ? (
            <Text style={[styles.position, { color: colors.text, opacity: 0.6 }]}>
              {player.position}
            </Text>
          ) : null}
        </Pressable>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  list: { borderRadius: 8, borderWidth: 1, overflow: "hidden" },
  row: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 14,
    paddingVertical: 12,
    gap: 12,
  },
  number: { width: 32, fontSize: 13, fontVariant: ["tabular-nums"] },
  name: { flex: 1, fontSize: 15, fontWeight: "500" },
  position: { fontSize: 12 },
  empty: { padding: 24, alignItems: "center" },
});
