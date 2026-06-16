import { useMutation } from "@tanstack/react-query";

import { apiClient } from "../api/client";

export const REPORT_REASONS = [
  { key: "ABUSE", label: "욕설/혐오" },
  { key: "SPAM", label: "스팸/광고" },
  { key: "SEXUAL", label: "음란물" },
  { key: "ETC", label: "기타" },
] as const;

interface ReportInput {
  targetType: "POST" | "COMMENT";
  targetId: number;
  reason: string;
}

export function useReport() {
  return useMutation({
    mutationFn: async (input: ReportInput) => {
      await apiClient.post("/api/reports", input);
    },
  });
}

export function useBlock() {
  return useMutation({
    mutationFn: async (blockedId: number) => {
      await apiClient.post("/api/blocks", { blockedId });
    },
  });
}
