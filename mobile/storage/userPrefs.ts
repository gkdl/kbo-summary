import AsyncStorage from "@react-native-async-storage/async-storage";

const KEYS = {
  myTeam: "kbo.myTeam",
  onboardingDone: "kbo.isOnboardingDone",
} as const;

export interface UserPrefs {
  myTeam: string | null;
  isOnboardingDone: boolean;
}

export const userPrefs = {
  async load(): Promise<UserPrefs> {
    const [myTeam, onboardingDone] = await Promise.all([
      AsyncStorage.getItem(KEYS.myTeam),
      AsyncStorage.getItem(KEYS.onboardingDone),
    ]);
    return {
      myTeam: myTeam,
      isOnboardingDone: onboardingDone === "true",
    };
  },

  async setMyTeam(teamCode: string | null): Promise<void> {
    if (teamCode === null) {
      await AsyncStorage.removeItem(KEYS.myTeam);
    } else {
      await AsyncStorage.setItem(KEYS.myTeam, teamCode);
    }
  },

  async setOnboardingDone(done: boolean): Promise<void> {
    await AsyncStorage.setItem(KEYS.onboardingDone, String(done));
  },

  async clear(): Promise<void> {
    await AsyncStorage.multiRemove([KEYS.myTeam, KEYS.onboardingDone]);
  },
};
