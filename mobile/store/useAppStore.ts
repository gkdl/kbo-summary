import { create } from "zustand";

interface HydratePayload {
  myTeam: string | null;
  isOnboardingDone: boolean;
}

interface AppState {
  myTeam: string | null;
  isOnboardingDone: boolean;
  isHydrated: boolean;
  setMyTeam: (teamCode: string | null) => void;
  setOnboardingDone: (done: boolean) => void;
  hydrate: (payload: HydratePayload) => void;
}

export const useAppStore = create<AppState>()((set) => ({
  myTeam: null,
  isOnboardingDone: false,
  isHydrated: false,
  setMyTeam: (teamCode) => set({ myTeam: teamCode }),
  setOnboardingDone: (done) => set({ isOnboardingDone: done }),
  hydrate: (payload) => set({ ...payload, isHydrated: true }),
}));
