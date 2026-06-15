import { useInfiniteQuery, useQuery } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { PostDetail, PostListResponse } from "../types/community";

interface PostsParams {
  team: string | null;
  sort: "latest" | "popular";
}

export function usePosts({ team, sort }: PostsParams) {
  return useInfiniteQuery({
    queryKey: ["posts", team, sort],
    initialPageParam: 0,
    queryFn: async ({ pageParam }) => {
      const res = await apiClient.get<ApiResponse<PostListResponse>>("/api/posts", {
        params: {
          team: team ?? undefined,
          sort,
          page: pageParam,
        },
      });
      return res.data.data;
    },
    getNextPageParam: (lastPage) =>
      lastPage?.hasNext ? (lastPage.page ?? 0) + 1 : undefined,
  });
}

export function usePost(postId: string) {
  return useQuery({
    queryKey: ["post", postId],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PostDetail>>(`/api/posts/${postId}`);
      return res.data.data;
    },
    enabled: postId.length > 0,
  });
}
