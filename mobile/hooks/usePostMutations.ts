import { useMutation, useQueryClient } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";

interface CreatePostInput {
  teamCode: string;
  title: string;
  content: string;
}

export function useCreatePost() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (input: CreatePostInput) => {
      const res = await apiClient.post<ApiResponse<{ postId: number }>>("/api/posts", input);
      return res.data.data;
    },
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["posts"] });
    },
  });
}

export function useDeletePost() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (postId: number) => {
      await apiClient.delete(`/api/posts/${postId}`);
    },
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["posts"] });
    },
  });
}
