import { useQuery } from "@tanstack/react-query";
import { apiClient } from "../api/client";
import type { ApiResponse, GameHighlight } from "../types/game";

/**
 * 특정 날짜의 종료된 경기 하이라이트 목록.
 * 하이라이트 영상이 없는 경기는 서버에서 이미 제외됨.
 */
export function useHighlightsByDate(date: string) {
  return useQuery({
    queryKey: ["highlights", date],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<GameHighlight[]>>(
        "/api/games/highlights",
        { params: { date } },
      );
      return res.data.data;
    },
    enabled: date.length === 8,
    staleTime: 5 * 60_000, // 5분 — 같은 날짜 자주 다시 조회되지 않게
  });
}
