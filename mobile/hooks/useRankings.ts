import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { Standing } from "../types/team";

export function useRankings() {
  return useQuery({
    queryKey: ["rankings"],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<Standing[]>>("/api/rankings");
      return res.data.data ?? [];
    },
  });
}
