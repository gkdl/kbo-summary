export interface TeamInfo {
  code: string;
  name: string;
  shortName: string;
  color: string;
}

// 팀 코드는 STEP 4 백엔드 KBO_TEAM_CODES 와 맞춤.
// 단, 백엔드가 KIA·한화를 모두 "HH"로 두어 충돌하므로 KIA는 "HT"로 분리했다 (백엔드 수정 필요).
export const TEAMS: TeamInfo[] = [
  { code: "LG", name: "LG 트윈스", shortName: "LG", color: "#E8175D" },
  { code: "KT", name: "KT 위즈", shortName: "KT", color: "#E60012" },
  { code: "SK", name: "SSG 랜더스", shortName: "SSG", color: "#CE0E2D" },
  { code: "NC", name: "NC 다이노스", shortName: "NC", color: "#3E7CC2" },
  { code: "OB", name: "두산 베어스", shortName: "두산", color: "#4158B0" },
  { code: "HT", name: "KIA 타이거즈", shortName: "KIA", color: "#EA0029" },
  { code: "LT", name: "롯데 자이언츠", shortName: "롯데", color: "#1565C0" },
  { code: "SS", name: "삼성 라이온즈", shortName: "삼성", color: "#1976D2" },
  { code: "HH", name: "한화 이글스", shortName: "한화", color: "#FC4E00" },
  { code: "WO", name: "키움 히어로즈", shortName: "키움", color: "#C62828" },
];

export const TEAMS_BY_CODE: Record<string, TeamInfo> = Object.fromEntries(
  TEAMS.map((team) => [team.code, team]),
);

export function getTeam(code: string): TeamInfo | undefined {
  return TEAMS_BY_CODE[code];
}
