import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { PlayerRanking } from "../types/player";

// type: "hitter" | "pitcher" (경로), category: 지표 (쿼리 파라미터 type)
export function usePlayerRankings(category: string, type: string) {
  return useQuery({
    queryKey: ["playerRankings", type, category],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PlayerRanking[]>>(
        `/api/players/rankings/${type}`,
        { params: { type: category } },
      );
      return res.data.data ?? [];
    },
    enabled: category.length > 0 && type.length > 0,
  });
}
