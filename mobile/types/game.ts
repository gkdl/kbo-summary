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

export interface GameDetail {
  game: Game;
  inningScores: InningScore[];
  homeLine: TeamLine;
  awayLine: TeamLine;
}

export interface GameSummary {
  gameId: string;
  summary: string;
  createdAt: string;
}
