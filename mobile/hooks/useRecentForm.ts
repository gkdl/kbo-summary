import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { RecentForm } from "../types/team";

export function useRecentForm(teamCode: string) {
  return useQuery({
    queryKey: ["recentForm", teamCode],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<RecentForm>>(
        `/api/teams/${teamCode}/recent-form`,
      );
      return res.data.data;
    },
    enabled: teamCode.length > 0,
  });
}
