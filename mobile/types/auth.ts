export interface Member {
  memberId: number;
  nickname: string;
  teamCode: string | null;
  role: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  member: Member;
}
