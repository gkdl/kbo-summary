import { useState } from "react";

import { apiClient } from "../api/client";
import { loginWithKakao, logoutKakao, unlinkKakao } from "../lib/kakao";
import { useAuthStore } from "../store/useAuthStore";

/**
 * 인증 액션 모음. 화면에서 login/logout/withdraw 를 호출하면 스토어·SecureStore 가 함께 갱신된다.
 */
export function useAuth() {
  const member = useAuthStore((s) => s.member);
  const isAuthed = useAuthStore((s) => s.accessToken != null && s.member != null);
  const setAuth = useAuthStore((s) => s.setAuth);
  const clearAuth = useAuthStore((s) => s.clearAuth);
  const [loading, setLoading] = useState(false);

  const login = async () => {
    setLoading(true);
    try {
      const data = await loginWithKakao();
      await setAuth(data);
      return data;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    await logoutKakao();
    await clearAuth();
  };

  const withdraw = async () => {
    try {
      await apiClient.delete("/api/members/me");
    } finally {
      // 탈퇴 시 카카오 연결 해제 — 재로그인 때 동의를 다시 받도록 (unlink 가 세션도 해제)
      await unlinkKakao();
      await clearAuth();
    }
  };

  return { member, isAuthed, loading, login, logout, withdraw };
}
