import { StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";

// KBO 박스스코어 응답을 그대로 옮긴 타입.
// playerId 는 KBO 가 박스스코어에 노출하지 않으므로 이름만 표시한다.
export interface HitterLine {
  playerName: string;
  position?: string | null;
  battingOrder?: number | null;
  teamCode: string;
  atBats: number;
  hits: number;
  rbi: number;
  runs: number;
  avg?: string | number | null;
}

export interface PitcherLine {
  playerName: string;
  teamCode: string;
  role?: string | null;
  decision?: string | null;
  inningsPitched?: string | number | null;
  pitchCount: number;
  atBats: number;
  hits: number;
  homeRuns: number;
  walks: number;
  strikeOuts: number;
  earnedRuns: number;
  era?: string | number | null;
}

interface Props {
  awayTeamCode: string;
  homeTeamCode: string;
  awayHitters: HitterLine[];
  homeHitters: HitterLine[];
  awayPitchers: PitcherLine[];
  homePitchers: PitcherLine[];
}

export function BoxScoreTable({
  awayTeamCode,
  homeTeamCode,
  awayHitters,
  homeHitters,
  awayPitchers,
  homePitchers,
}: Props) {
  const { colors } = useTheme();
  const away = getTeam(awayTeamCode);
  const home = getTeam(homeTeamCode);

  const hitterHeader = ["선수", "AB", "H", "RBI", "R", "AVG"];
  const pitcherHeader = ["선수", "결과", "IP", "NP", "H", "HR", "BB", "SO", "ER", "ERA"];

  return (
    <View style={{ gap: 16 }}>
      <Text style={[styles.groupTitle, { color: colors.text }]}>타자</Text>
      <TeamHitterSection
        label={`${away?.shortName ?? awayTeamCode} (원정)`}
        accent={away?.color ?? colors.primary}
        hitters={awayHitters}
        header={hitterHeader}
        colors={colors}
      />
      <TeamHitterSection
        label={`${home?.shortName ?? homeTeamCode} (홈)`}
        accent={home?.color ?? colors.primary}
        hitters={homeHitters}
        header={hitterHeader}
        colors={colors}
      />

      <Text style={[styles.groupTitle, { color: colors.text, marginTop: 4 }]}>투수</Text>
      <TeamPitcherSection
        label={`${away?.shortName ?? awayTeamCode} (원정)`}
        accent={away?.color ?? colors.primary}
        pitchers={awayPitchers}
        header={pitcherHeader}
        colors={colors}
      />
      <TeamPitcherSection
        label={`${home?.shortName ?? homeTeamCode} (홈)`}
        accent={home?.color ?? colors.primary}
        pitchers={homePitchers}
        header={pitcherHeader}
        colors={colors}
      />
    </View>
  );
}

interface ColorSet {
  text: string;
  border: string;
  card: string;
}

function TeamHitterSection({
  label,
  accent,
  hitters,
  header,
  colors,
}: {
  label: string;
  accent: string;
  hitters: HitterLine[];
  header: string[];
  colors: ColorSet;
}) {
  return (
    <View>
      <SectionLabel label={label} accent={accent} color={colors.text} />
      <View style={[styles.table, { borderColor: colors.border, backgroundColor: colors.card }]}>
        <Row cells={header} colors={colors} header />
        {hitters.length === 0 ? (
          <EmptyRow colors={colors} text="기록 없음" />
        ) : (
          hitters.map((h, idx) => (
            <Row
              key={`${h.playerName}-${idx}`}
              colors={colors}
              cells={[
                h.playerName + (h.position ? ` (${h.position})` : ""),
                h.atBats,
                h.hits,
                h.rbi,
                h.runs,
                fmt(h.avg),
              ]}
            />
          ))
        )}
      </View>
    </View>
  );
}

function TeamPitcherSection({
  label,
  accent,
  pitchers,
  header,
  colors,
}: {
  label: string;
  accent: string;
  pitchers: PitcherLine[];
  header: string[];
  colors: ColorSet;
}) {
  return (
    <View>
      <SectionLabel label={label} accent={accent} color={colors.text} />
      <View style={[styles.table, { borderColor: colors.border, backgroundColor: colors.card }]}>
        <Row cells={header} colors={colors} header />
        {pitchers.length === 0 ? (
          <EmptyRow colors={colors} text="기록 없음" />
        ) : (
          pitchers.map((p, idx) => (
            <Row
              key={`${p.playerName}-${idx}`}
              colors={colors}
              cells={[
                p.playerName,
                p.decision ?? p.role ?? "",
                fmt(p.inningsPitched),
                p.pitchCount,
                p.hits,
                p.homeRuns,
                p.walks,
                p.strikeOuts,
                p.earnedRuns,
                fmt(p.era),
              ]}
            />
          ))
        )}
      </View>
    </View>
  );
}

function SectionLabel({ label, accent, color }: { label: string; accent: string; color: string }) {
  return (
    <View style={styles.sectionLabelRow}>
      <View style={[styles.sectionDot, { backgroundColor: accent }]} />
      <Text style={[styles.sectionLabel, { color }]}>{label}</Text>
    </View>
  );
}

function fmt(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

interface RowProps {
  colors: { text: string; border: string };
  cells: (string | number)[];
  header?: boolean;
}

function Row({ colors, cells, header }: RowProps) {
  return (
    <View style={[styles.row, { borderColor: colors.border }]}>
      {cells.map((value, index) => (
        <Text
          key={index}
          style={[
            styles.cell,
            index === 0 ? styles.nameCell : null,
            {
              color: colors.text,
              fontWeight: header || index === 0 ? "600" : "400",
              opacity: header ? 0.7 : 1,
            },
          ]}
          numberOfLines={1}
        >
          {value}
        </Text>
      ))}
    </View>
  );
}

function EmptyRow({ colors, text }: { colors: { text: string; border: string }; text: string }) {
  return (
    <View style={[styles.row, { borderColor: colors.border, justifyContent: "center" }]}>
      <Text style={{ color: colors.subText, fontSize: 12, paddingVertical: 6 }}>{text}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  groupTitle: { fontSize: 15, fontWeight: "700" },
  sectionLabelRow: { flexDirection: "row", alignItems: "center", gap: 8, marginBottom: 6 },
  sectionDot: { width: 10, height: 10, borderRadius: 5 },
  sectionLabel: { fontSize: 13, fontWeight: "600" },
  table: { borderRadius: 8, borderWidth: 1, overflow: "hidden", marginBottom: 6 },
  row: {
    flexDirection: "row",
    paddingHorizontal: 8,
    paddingVertical: 8,
    borderTopWidth: StyleSheet.hairlineWidth,
    alignItems: "center",
  },
  cell: { flex: 1, textAlign: "center", fontSize: 11, fontVariant: ["tabular-nums"] },
  nameCell: { flex: 2, textAlign: "left", fontSize: 12 },
});
