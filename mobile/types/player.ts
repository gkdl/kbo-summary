export interface PlayerProfile {
  playerId: string;
  name: string;
  teamCode: string | null;
  playerType: string;
  position: string | null;
  backNumber: string | null;
  bats: string | null;
  throws: string | null;
  birthDate: string | null;
  height: number | null;
  weight: number | null;
  school: string | null;
  debutYear: number | null;
}

export interface HittingLine {
  avg: number | null;
  games: number;
  atBats: number;
  hits: number;
  homeRuns: number;
  rbi: number;
  runs: number;
  stolenBases: number;
  walks: number;
  strikeOuts: number;
  ops: number | null;
}

export interface PitchingLine {
  era: number | null;
  games: number;
  wins: number;
  losses: number;
  saves: number;
  holds: number;
  inningsPitched: number | null;
  hits: number;
  strikeOuts: number;
  walks: number;
  whip: number | null;
}

export interface PlayerStat {
  playerId: string;
  season: number;
  playerType: string;
  hitting: HittingLine | null;
  pitching: PitchingLine | null;
}

export interface PlayerSearchResult {
  playerId: string;
  name: string;
  teamCode: string | null;
  playerType: string;
  position: string | null;
}

export interface PlayerRanking {
  rank: number;
  playerId: string;
  playerName: string;
  teamCode: string | null;
  category: string;
  value: string;
}
