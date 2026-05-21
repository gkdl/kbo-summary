import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { TeamDetail } from "../types/team";

export function useTeam(teamCode: string) {
  return useQuery({
    queryKey: ["team", teamCode],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<TeamDetail>>(`/api/teams/${teamCode}`);
      return res.data.data;
    },
    enabled: teamCode.length > 0,
  });
}
