import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse, GameSummary } from "../types/game";

export function useGameSummary(gameId: string) {
  return useQuery({
    queryKey: ["gameSummary", gameId],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<GameSummary>>(
        `/api/games/${gameId}/summary`,
      );
      return res.data.data;
    },
    enabled: gameId.length > 0,
  });
}
