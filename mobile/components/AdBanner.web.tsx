// Web 빌드 전용 stub — react-native-google-mobile-ads 는 native 전용이라 web 에서는 광고를 표시하지 않는다.
// Metro 가 .web.tsx 파일을 web 번들에 자동 선택하므로 native 모듈 import 자체를 회피한다.
export function AdBanner() {
  return null;
}
