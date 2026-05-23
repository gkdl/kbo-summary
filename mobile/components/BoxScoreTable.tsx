import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";
import { useTheme } from "@react-navigation/native";

// 박스스코어용 로컬 타입. API에서 노출되면 mobile/types 로 옮긴다.
export interface HitterLine {
  playerId: string;
  playerName: string;
  position?: string | null;
  atBats: number;
  runs: number;
  hits: number;
  rbi: number;
  homeRuns: number;
  walks: number;
  strikeOuts: number;
}

export interface PitcherLine {
  playerId: string;
  playerName: string;
  inningsPitched?: string | null;
  hits: number;
  runs: number;
  earnedRuns: number;
  walks: number;
  strikeOuts: number;
  decision?: string | null;
}

interface Props {
  hitters: HitterLine[];
  pitchers: PitcherLine[];
}

export function BoxScoreTable({ hitters, pitchers }: Props) {
  const { colors } = useTheme();
  const router = useRouter();

  return (
    <View style={{ gap: 16 }}>
      <Section title="타자" colors={colors}>
        <Row
          colors={colors}
          header
          cells={["선수", "AB", "R", "H", "RBI", "HR", "BB", "SO"]}
        />
        {hitters.map((h, idx) => (
          <Row
            key={h.playerId + idx}
            colors={colors}
            onPress={() => router.push(`/player/${h.playerId}`)}
            cells={[
              h.playerName + (h.position ? ` (${h.position})` : ""),
              h.atBats,
              h.runs,
              h.hits,
              h.rbi,
              h.homeRuns,
              h.walks,
              h.strikeOuts,
            ]}
          />
        ))}
      </Section>

      <Section title="투수" colors={colors}>
        <Row
          colors={colors}
          header
          cells={["선수", "IP", "H", "R", "ER", "BB", "SO", "결과"]}
        />
        {pitchers.map((p, idx) => (
          <Row
            key={p.playerId + idx}
            colors={colors}
            onPress={() => router.push(`/player/${p.playerId}`)}
            cells={[
              p.playerName,
              p.inningsPitched ?? "-",
              p.hits,
              p.runs,
              p.earnedRuns,
              p.walks,
              p.strikeOuts,
              p.decision ?? "",
            ]}
          />
        ))}
      </Section>
    </View>
  );
}

interface SectionProps {
  title: string;
  colors: { text: string; border: string; card: string };
  children: React.ReactNode;
}

function Section({ title, colors, children }: SectionProps) {
  return (
    <View>
      <Text style={[styles.sectionTitle, { color: colors.text }]}>{title}</Text>
      <View style={[styles.table, { borderColor: colors.border, backgroundColor: colors.card }]}>
        {children}
      </View>
    </View>
  );
}

interface RowProps {
  colors: { text: string; border: string };
  cells: Array<string | number>;
  header?: boolean;
  onPress?: () => void;
}

function Row({ colors, cells, header, onPress }: RowProps) {
  const content = cells.map((value, index) => (
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
  ));

  if (header) {
    return <View style={[styles.row, { borderColor: colors.border }]}>{content}</View>;
  }

  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        styles.row,
        { borderColor: colors.border, opacity: pressed ? 0.6 : 1 },
      ]}
    >
      {content}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  sectionTitle: { fontSize: 14, fontWeight: "600", marginBottom: 6 },
  table: { borderRadius: 8, borderWidth: 1, overflow: "hidden" },
  row: {
    flexDirection: "row",
    paddingHorizontal: 10,
    paddingVertical: 8,
    borderTopWidth: StyleSheet.hairlineWidth,
    alignItems: "center",
  },
  cell: { flex: 1, textAlign: "center", fontSize: 12, fontVariant: ["tabular-nums"] },
  nameCell: { flex: 2.2, textAlign: "left" },
});
