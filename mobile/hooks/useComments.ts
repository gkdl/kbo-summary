import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiClient } from "../api/client";
import type { ApiResponse } from "../types/game";
import type { Comment, LikeResult } from "../types/comment";

export function useComments(postId: string) {
  return useQuery({
    queryKey: ["comments", postId],
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<Comment[]>>(`/api/posts/${postId}/comments`);
      return res.data.data ?? [];
    },
    enabled: postId.length > 0,
  });
}

export function useCreateComment(postId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (input: { content: string; parentId: number | null }) => {
      await apiClient.post(`/api/posts/${postId}/comments`, input);
    },
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["comments", postId] });
      void qc.invalidateQueries({ queryKey: ["post", postId] });
    },
  });
}

export function useDeleteComment(postId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (commentId: number) => {
      await apiClient.delete(`/api/comments/${commentId}`);
    },
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["comments", postId] });
      void qc.invalidateQueries({ queryKey: ["post", postId] });
    },
  });
}

export function useToggleLike(postId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const res = await apiClient.post<ApiResponse<LikeResult>>(`/api/posts/${postId}/like`);
      return res.data.data;
    },
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["post", postId] });
    },
  });
}
