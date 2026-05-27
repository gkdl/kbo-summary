// Native (iOS/Android) 전용 AdMob 초기화.
// web 번들에서는 initAds.web.ts 가 자동으로 선택돼 no-op 가 실행된다.
import mobileAds from "react-native-google-mobile-ads";

export async function initAds(): Promise<void> {
  try {
    const adapterStatuses = await mobileAds().initialize();
    console.log("[AdMob] initialized", adapterStatuses);
  } catch (err) {
    console.warn("[AdMob] init failed", err);
  }
}
