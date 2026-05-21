import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { PlayerGameLog } from "../types/player";

export function usePlayerGameLog(playerId: string) {
  return useQuery({
    queryKey: ["playerGameLog", playerId],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PlayerGameLog>>(
        `/api/players/${playerId}/gamelog`,
      );
      return res.data.data;
    },
    enabled: playerId.length > 0,
  });
}
