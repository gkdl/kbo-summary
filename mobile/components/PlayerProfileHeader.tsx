import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";
import { border, radius, spacing } from "../constants/tokens";
import type { PlayerProfile } from "../types/player";

interface Props {
  profile: PlayerProfile;
}

export function PlayerProfileHeader({ profile }: Props) {
  const { colors } = useTheme();
  const team = profile.teamCode ? getTeam(profile.teamCode) : undefined;
  const accent = team?.color ?? colors.primary;

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <View style={styles.topRow}>
        <View style={[styles.badge, { backgroundColor: accent }]}>
          <Text style={styles.badgeNumber}>{profile.backNumber ?? "-"}</Text>
        </View>
        <View style={{ flex: 1 }}>
          <Text style={[styles.name, { color: colors.text }]}>{profile.name}</Text>
          <Text style={[styles.subtitle, { color: colors.subText }]}>
            {team?.name ?? profile.teamCode ?? ""}
            {profile.position ? ` · ${profile.position}` : ""}
            {profile.playerType ? ` · ${profile.playerType === "PITCHER" ? "투수" : "타자"}` : ""}
          </Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: spacing.lg, borderRadius: radius.md, borderWidth: border.card },
  topRow: { flexDirection: "row", alignItems: "center", gap: spacing.md + 2 },
  badge: {
    width: 56,
    height: 56,
    borderRadius: radius.pill,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: border.card,
    borderColor: "rgba(255,255,255,0.2)",
  },
  badgeNumber: { color: "#FFFFFF", fontSize: 22, fontWeight: "700" },
  name: { fontSize: 20, fontWeight: "700" },
  subtitle: { fontSize: 13, marginTop: 2 },
});
