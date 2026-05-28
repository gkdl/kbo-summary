export interface ApiError {
  code: string;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: ApiError | null;
  cachedAt: string | null;
}

export interface Game {
  gameId: string;
  gameDate: string;
  homeTeamCode: string;
  awayTeamCode: string;
  homeScore: number | null;
  awayScore: number | null;
  status: string;
  stadium: string | null;
  startTime: string | null;
}

export interface InningScore {
  inning: number;
  homeRuns: number;
  awayRuns: number;
}

export interface TeamLine {
  runs: number;
  hits: number;
  errors: number;
  walks: number;
}

export interface BoxHitter {
  playerName: string;
  battingOrder: number | null;
  position: string | null;
  teamCode: string;
  atBats: number;
  hits: number;
  rbi: number;
  runs: number;
  avg: string | number | null;
}

export interface BoxPitcher {
  playerName: string;
  teamCode: string;
  role: string | null;
  decision: string | null;
  inningsPitched: string | number | null;
  pitchCount: number;
  battersFaced: number;
  atBats: number;
  hits: number;
  homeRuns: number;
  walks: number;
  strikeOuts: number;
  runs: number;
  earnedRuns: number;
  era: string | number | null;
}

export interface Highlight {
  youtubeVideoId: string;
  title: string | null;
}

/** 하이라이트 탭 목록용 — 경기 메타 + 하이라이트 */
export interface GameHighlight {
  gameId: string;
  gameDate: string;
  awayTeamCode: string;
  homeTeamCode: string;
  awayScore: number | null;
  homeScore: number | null;
  highlight: Highlight;
}

export interface GameDetail {
  game: Game;
  inningScores: InningScore[];
  homeLine: TeamLine;
  awayLine: TeamLine;
  awayHitters: BoxHitter[];
  homeHitters: BoxHitter[];
  awayPitchers: BoxPitcher[];
  homePitchers: BoxPitcher[];
  highlight: Highlight | null;
}

export interface GameSummary {
  gameId: string;
  summary: string;
  createdAt: string;
}
