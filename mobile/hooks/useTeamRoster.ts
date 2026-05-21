import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { TeamRoster } from "../types/team";

export function useTeamRoster(teamCode: string) {
  return useQuery({
    queryKey: ["teamRoster", teamCode],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<TeamRoster>>(
        `/api/teams/${teamCode}/roster`,
      );
      return res.data.data;
    },
    enabled: teamCode.length > 0,
  });
}
