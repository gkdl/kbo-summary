import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse, GameDetail } from "../types/game";

export function useGameDetail(gameId: string) {
  return useQuery({
    queryKey: ["gameDetail", gameId],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<GameDetail>>(`/api/games/${gameId}`);
      return res.data.data;
    },
    enabled: gameId.length > 0,
  });
}
