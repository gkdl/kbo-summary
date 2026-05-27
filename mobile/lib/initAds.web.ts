// Web 번들에서는 AdMob 이 의미 없으므로 no-op.
// Metro 번들러가 .web.ts 확장자를 자동으로 우선 선택한다.
export async function initAds(): Promise<void> {
  // intentionally empty
}
