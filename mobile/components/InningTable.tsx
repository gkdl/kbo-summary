import { ScrollView, StyleSheet, Text, View } from "react-native";
import { useTheme } from "../hooks/useTheme";

import { getTeam } from "../constants/teams";
import type { InningScore, TeamLine } from "../types/game";

interface Props {
  awayTeamCode: string;
  homeTeamCode: string;
  innings: InningScore[];
  awayLine?: TeamLine;
  homeLine?: TeamLine;
}

export function InningTable({ awayTeamCode, homeTeamCode, innings, awayLine, homeLine }: Props) {
  const { colors } = useTheme();
  const away = getTeam(awayTeamCode);
  const home = getTeam(homeTeamCode);
  const awayRuns = awayLine?.runs ?? innings.reduce((sum, i) => sum + i.awayRuns, 0);
  const homeRuns = homeLine?.runs ?? innings.reduce((sum, i) => sum + i.homeRuns, 0);

  return (
    <View style={[styles.wrapper, { borderColor: colors.border, backgroundColor: colors.card }]}>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        // horizontal ScrollView 가 column 부모 안에서 세로 stretch 되지 않도록 flexGrow:0
        style={styles.scroll}
      >
        <View>
        <View style={[styles.row, styles.header, { borderBottomColor: colors.border }]}>
          <Cell style={styles.teamCell} color={colors.text} bold>
            팀
          </Cell>
          {innings.map((inning) => (
            <Cell key={inning.inning} color={colors.text} bold>
              {inning.inning}
            </Cell>
          ))}
          <Cell color={colors.text} bold style={styles.totalCell}>R</Cell>
          <Cell color={colors.text} bold style={styles.totalCell}>H</Cell>
          <Cell color={colors.text} bold style={styles.totalCell}>E</Cell>
          <Cell color={colors.text} bold style={styles.totalCell}>B</Cell>
        </View>

        <View style={[styles.row, { borderBottomColor: colors.border }]}>
          <Cell style={styles.teamCell} color={away?.color ?? colors.text} bold>
            {away?.shortName ?? awayTeamCode}
          </Cell>
          {innings.map((inning) => (
            <Cell key={inning.inning} color={colors.text}>
              {inning.awayRuns}
            </Cell>
          ))}
          <Cell color={colors.text} bold style={styles.totalCell}>{awayRuns}</Cell>
          <Cell color={colors.text} style={styles.totalCell}>{awayLine?.hits ?? "-"}</Cell>
          <Cell color={colors.text} style={styles.totalCell}>{awayLine?.errors ?? "-"}</Cell>
          <Cell color={colors.text} style={styles.totalCell}>{awayLine?.walks ?? "-"}</Cell>
        </View>

        <View style={styles.row}>
          <Cell style={styles.teamCell} color={home?.color ?? colors.text} bold>
            {home?.shortName ?? homeTeamCode}
          </Cell>
          {innings.map((inning) => (
            <Cell key={inning.inning} color={colors.text}>
              {inning.homeRuns}
            </Cell>
          ))}
          <Cell color={colors.text} bold style={styles.totalCell}>{homeRuns}</Cell>
          <Cell color={colors.text} style={styles.totalCell}>{homeLine?.hits ?? "-"}</Cell>
          <Cell color={colors.text} style={styles.totalCell}>{homeLine?.errors ?? "-"}</Cell>
          <Cell color={colors.text} style={styles.totalCell}>{homeLine?.walks ?? "-"}</Cell>
        </View>
        </View>
      </ScrollView>
    </View>
  );
}

interface CellProps {
  children: React.ReactNode;
  color: string;
  bold?: boolean;
  style?: object;
}

function Cell({ children, color, bold, style }: CellProps) {
  return (
    <View style={[styles.cell, style]}>
      <Text style={[styles.cellText, { color, fontWeight: bold ? "700" : "400" }]}>
        {children}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  // 외부 View 가 화면 폭 안에 들어가도록 width:100%, overflow:hidden 으로 horizontal scroll 영역을 가둔다
  wrapper: { borderRadius: 8, borderWidth: 1, width: "100%", overflow: "hidden" },
  scroll: { flexGrow: 0 },
  row: {
    flexDirection: "row",
    alignItems: "center",
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  header: {},
  cell: { width: 32, paddingVertical: 10, alignItems: "center", justifyContent: "center" },
  cellText: { fontSize: 13, fontVariant: ["tabular-nums"] },
  teamCell: { width: 64, alignItems: "flex-start", paddingLeft: 10 },
  totalCell: { width: 36 },
});
