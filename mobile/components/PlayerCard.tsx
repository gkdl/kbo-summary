import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";

interface PlayerCardData {
  playerId: string;
  name: string;
  teamCode?: string | null;
  position?: string | null;
  backNumber?: string | null;
  playerType?: string;
}

interface Props {
  player: PlayerCardData;
}

export function PlayerCard({ player }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const team = player.teamCode ? getTeam(player.teamCode) : undefined;

  return (
    <Pressable
      onPress={() => router.push(`/player/${player.playerId}`)}
      style={({ pressed }) => [
        styles.card,
        { backgroundColor: colors.card, borderColor: colors.border, opacity: pressed ? 0.7 : 1 },
      ]}
    >
      <View style={[styles.accent, { backgroundColor: team?.color ?? colors.primary }]}>
        <Text style={styles.number}>{player.backNumber ?? "-"}</Text>
      </View>
      <View style={styles.content}>
        <Text style={[styles.name, { color: colors.text }]}>{player.name}</Text>
        <Text style={[styles.meta, { color: colors.text, opacity: 0.6 }]}>
          {team?.shortName ?? player.teamCode ?? ""}
          {player.position ? ` · ${player.position}` : ""}
        </Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { flexDirection: "row", borderRadius: 8, borderWidth: 1, overflow: "hidden" },
  accent: { width: 48, alignItems: "center", justifyContent: "center" },
  number: { color: "#FFFFFF", fontSize: 18, fontWeight: "700" },
  content: { flex: 1, padding: 12, gap: 2 },
  name: { fontSize: 16, fontWeight: "600" },
  meta: { fontSize: 12 },
});
