import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { PlayerSearchResult } from "../types/player";

// 검색어는 2글자 이상일 때만 요청한다 (백엔드와 동일 규칙)
export function usePlayerSearch(keyword: string) {
  return useQuery({
    queryKey: ["playerSearch", keyword],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PlayerSearchResult[]>>(
        "/api/players/search",
        { params: { q: keyword } },
      );
      return res.data.data ?? [];
    },
    enabled: keyword.trim().length >= 2,
  });
}
