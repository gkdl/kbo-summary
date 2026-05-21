import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { PlayerProfile } from "../types/player";

export function usePlayer(playerId: string) {
  return useQuery({
    queryKey: ["player", playerId],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PlayerProfile>>(
        `/api/players/${playerId}`,
      );
      return res.data.data;
    },
    enabled: playerId.length > 0,
  });
}
