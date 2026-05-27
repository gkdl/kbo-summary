import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse, GameSummary } from "../types/game";

// 종료된 경기에서만 호출 가능. 진행 중/예정 경기는 백엔드가 SummaryException(500) 을 던지므로
// 호출자가 game.status === "FINISHED" 일 때만 enabled 를 true 로 넘긴다.
export function useGameSummary(gameId: string, enabled: boolean = true) {
  return useQuery({
    queryKey: ["gameSummary", gameId],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<GameSummary>>(
        `/api/games/${gameId}/summary`,
      );
      return res.data.data;
    },
    enabled: enabled && gameId.length > 0,
  });
}
