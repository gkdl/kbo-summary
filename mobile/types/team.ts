import type { Game } from "./game";

export interface TeamDetail {
  teamCode: string;
  teamName: string;
  stadium: string | null;
  teamColor: string | null;
  rank: number | null;
  wins: number;
  losses: number;
  draws: number;
}

export interface RosterPlayer {
  playerId: string;
  name: string;
  backNumber: string | null;
  position: string | null;
}

export interface TeamRoster {
  teamCode: string;
  players: RosterPlayer[];
}

export interface TeamStats {
  teamCode: string;
  season: number;
  rank: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number | null;
  gamesBehind: number | null;
}

export interface HeadToHead {
  teamA: string;
  teamB: string;
  teamAWins: number;
  teamBWins: number;
  draws: number;
  games: Game[];
}

export interface RecentForm {
  teamCode: string;
  recentForm: string[];
}

export interface Standing {
  rank: number;
  teamCode: string;
  season: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number | null;
  gamesBehind: number | null;
}
