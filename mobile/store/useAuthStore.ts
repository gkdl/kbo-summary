import { create } from "zustand";
import * as SecureStore from "expo-secure-store";

import type { Member, TokenResponse } from "../types/auth";

const STORAGE_KEY = "kbo.auth";

interface PersistedAuth {
  accessToken: string;
  refreshToken: string;
  member: Member;
}

interface AuthState {
  member: Member | null;
  accessToken: string | null;
  refreshToken: string | null;
  isHydrated: boolean;
  // 앱 시작 시 SecureStore 에서 토큰 복원
  hydrate: () => Promise<void>;
  // 로그인/토큰갱신 결과 저장
  setAuth: (data: TokenResponse) => Promise<void>;
  // 토큰만 갱신(회원정보 유지)
  setTokens: (accessToken: string, refreshToken: string) => Promise<void>;
  // 마이팀 등 회원정보 갱신
  setMember: (member: Member) => void;
  // 로그아웃/탈퇴 — 토큰 폐기
  clearAuth: () => Promise<void>;
}

async function persist(data: PersistedAuth | null) {
  if (data === null) {
    await SecureStore.deleteItemAsync(STORAGE_KEY);
  } else {
    await SecureStore.setItemAsync(STORAGE_KEY, JSON.stringify(data));
  }
}

export const useAuthStore = create<AuthState>()((set, get) => ({
  member: null,
  accessToken: null,
  refreshToken: null,
  isHydrated: false,

  hydrate: async () => {
    try {
      const raw = await SecureStore.getItemAsync(STORAGE_KEY);
      if (raw) {
        const data = JSON.parse(raw) as PersistedAuth;
        set({
          member: data.member,
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
        });
      }
    } catch {
      // 손상된 값이면 무시하고 비로그인 상태로
    } finally {
      set({ isHydrated: true });
    }
  },

  setAuth: async (data) => {
    set({ member: data.member, accessToken: data.accessToken, refreshToken: data.refreshToken });
    await persist({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      member: data.member,
    });
  },

  setTokens: async (accessToken, refreshToken) => {
    const member = get().member;
    set({ accessToken, refreshToken });
    if (member) await persist({ accessToken, refreshToken, member });
  },

  setMember: (member) => {
    const { accessToken, refreshToken } = get();
    set({ member });
    if (accessToken && refreshToken) {
      void persist({ accessToken, refreshToken, member });
    }
  },

  clearAuth: async () => {
    set({ member: null, accessToken: null, refreshToken: null });
    await persist(null);
  },
}));
