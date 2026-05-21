import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { HeadToHead } from "../types/team";

export function useHeadToHead(teamA: string, teamB: string) {
  return useQuery({
    queryKey: ["headToHead", teamA, teamB],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<HeadToHead>>(
        `/api/teams/${teamA}/head-to-head/${teamB}`,
      );
      return res.data.data;
    },
    enabled: teamA.length > 0 && teamB.length > 0,
  });
}
