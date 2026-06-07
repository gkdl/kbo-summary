export interface PlatformVersionInfo {
  minVersion: number;
  latestVersion: number;
  storeUrl: string;
}

export interface AppVersionResponse {
  android: PlatformVersionInfo;
  ios: PlatformVersionInfo;
}
