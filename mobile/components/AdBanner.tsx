import { Platform, StyleSheet, View } from "react-native";
import { BannerAd, BannerAdSize, TestIds } from "react-native-google-mobile-ads";

/**
 * Google AdMob 배너 광고 (Expo SDK 56 / react-native-google-mobile-ads v16).
 *
 * - native module 이라 Expo Go 에서는 동작 안 함 (dev build / EAS build 필요)
 * - web 빌드에서는 AdBanner.web.tsx 가 자동 선택되어 null 반환
 * - 개발 빌드(__DEV__=true) 에서는 Google 테스트 광고, 운영 빌드에서는 실제 광고 단위
 *
 * 운영 전 교체:
 *   - 아래 ANDROID_BANNER_UNIT_ID / IOS_BANNER_UNIT_ID 를 AdMob 콘솔에서 발급받은 실제 ID 로
 *   - 형식: "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY" (슬래시 `/` 구분자)
 */

// TODO: AdMob 콘솔 → 광고 단위 추가 → "배너" 형식으로 발급받은 후 교체
const ANDROID_BANNER_UNIT_ID = "ca-app-pub-6630409826466167/5671269274";
const IOS_BANNER_UNIT_ID = "ca-app-pub-6630409826466167/실제광고단위ID여기";

const realUnitId = Platform.OS === "ios" ? IOS_BANNER_UNIT_ID : ANDROID_BANNER_UNIT_ID;
const unitId = __DEV__ ? TestIds.BANNER : realUnitId;

export function AdBanner() {
  return (
    <View style={styles.wrapper}>
      <BannerAd
        unitId={unitId}
        size={BannerAdSize.ANCHORED_ADAPTIVE_BANNER}
        onAdLoaded={() => console.log("[AdBanner] loaded", unitId)}
        onAdFailedToLoad={(err) => console.warn("[AdBanner] failed", unitId, err)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: { alignItems: "center", paddingVertical: 4 },
});
