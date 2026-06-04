/**
 * 네비게이션·날짜바 등에서 사용하는 미니멀 아이콘.
 * TabIcons 와 동일하게 외부 폰트 없이 View + StyleSheet 만으로 그린다.
 */
import { StyleSheet, View } from "react-native";

interface Props {
  color: string;
  size?: number;
}

// 두 줄의 짧은 막대를 회전해 < 모양을 만든다.
// left  → 위 막대 `\`(+45deg), 아래 막대 `/`(-45deg) → 두 끝이 좌측에서 만나 `<`
// right → 그 반대
function Chevron({ color, size = 18, direction }: Props & { direction: "left" | "right" }) {
  const stroke = 2;
  const armLength = size * 0.45;
  const rotate = direction === "left" ? "45deg" : "-45deg";
  const rotateBottom = direction === "left" ? "-45deg" : "45deg";
  return (
    <View style={[styles.box, { width: size, height: size }]}>
      <View
        style={{
          position: "absolute",
          width: stroke,
          height: armLength,
          backgroundColor: color,
          borderRadius: stroke / 2,
          top: size / 2 - armLength + stroke / 2,
          left: size / 2 - stroke / 2,
          transform: [{ rotate }],
        }}
      />
      <View
        style={{
          position: "absolute",
          width: stroke,
          height: armLength,
          backgroundColor: color,
          borderRadius: stroke / 2,
          top: size / 2 - stroke / 2,
          left: size / 2 - stroke / 2,
          transform: [{ rotate: rotateBottom }],
        }}
      />
    </View>
  );
}

export function ChevronLeftIcon(props: Props) {
  return <Chevron {...props} direction="left" />;
}

export function ChevronRightIcon(props: Props) {
  return <Chevron {...props} direction="right" />;
}

// 윗 두 다리 + 사각 프레임 + 가로줄 1개로 캘린더 느낌
export function CalendarIcon({ color, size = 16 }: Props) {
  const stroke = 1.6;
  const frameTop = size * 0.2;
  const frameH = size - frameTop;
  const legH = size * 0.18;
  return (
    <View style={[styles.box, { width: size, height: size }]}>
      {/* 좌측 다리 */}
      <View
        style={{
          position: "absolute",
          width: stroke,
          height: legH,
          backgroundColor: color,
          borderRadius: stroke / 2,
          top: 0,
          left: size * 0.25,
        }}
      />
      {/* 우측 다리 */}
      <View
        style={{
          position: "absolute",
          width: stroke,
          height: legH,
          backgroundColor: color,
          borderRadius: stroke / 2,
          top: 0,
          right: size * 0.25,
        }}
      />
      {/* 프레임 */}
      <View
        style={{
          position: "absolute",
          top: frameTop,
          left: 0,
          width: size,
          height: frameH,
          borderWidth: stroke,
          borderColor: color,
          borderRadius: 2,
        }}
      />
      {/* 상단 구분선 */}
      <View
        style={{
          position: "absolute",
          top: frameTop + size * 0.22,
          left: 0,
          width: size,
          height: stroke,
          backgroundColor: color,
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  box: { alignItems: "center", justifyContent: "center" },
});
