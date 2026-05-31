import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";
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

      <View style={[styles.divider, { backgroundColor: colors.border }]} />

      <View style={styles.infoGrid}>
        <Info label="투타" value={`${profile.throws ?? "-"} / ${profile.bats ?? "-"}`} color={colors.text} />
        <Info label="생년월일" value={profile.birthDate ?? "-"} color={colors.text} />
        <Info
          label="신장/체중"
          value={
            profile.height || profile.weight
              ? `${profile.height ?? "-"}cm / ${profile.weight ?? "-"}kg`
              : "-"
          }
          color={colors.text}
        />
        <Info label="출신교" value={profile.school ?? "-"} color={colors.text} />
        <Info label="입단년도" value={profile.debutYear?.toString() ?? "-"} color={colors.text} />
      </View>
    </View>
  );
}

function Info({ label, value, color }: { label: string; value: string; color: string }) {
  return (
    <View style={styles.infoRow}>
      <Text style={[styles.infoLabel, { color, opacity: 0.6 }]}>{label}</Text>
      <Text style={[styles.infoValue, { color }]}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 16, borderRadius: 8, borderWidth: 1, gap: 12 },
  topRow: { flexDirection: "row", alignItems: "center", gap: 14 },
  badge: { width: 56, height: 56, borderRadius: 28, alignItems: "center", justifyContent: "center", borderWidth: 1, borderColor: "rgba(255,255,255,0.2)" },
  badgeNumber: { color: "#FFFFFF", fontSize: 22, fontWeight: "700" },
  name: { fontSize: 20, fontWeight: "700" },
  subtitle: { fontSize: 13, marginTop: 2 },
  divider: { height: StyleSheet.hairlineWidth },
  infoGrid: { gap: 6 },
  infoRow: { flexDirection: "row", justifyContent: "space-between" },
  infoLabel: { fontSize: 13 },
  infoValue: { fontSize: 13, fontWeight: "500" },
});
