import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse, Game } from "../types/game";

// date: yyyyMMdd 형식
export function useGames(date: string) {
  return useQuery({
    queryKey: ["games", date],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<Game[]>>("/api/games", {
        params: { date },
      });
      return res.data.data ?? [];
    },
    enabled: date.length > 0,
  });
}
