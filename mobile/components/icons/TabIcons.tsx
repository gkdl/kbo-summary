/**
 * 외부 폰트 의존 없이 View + StyleSheet 만으로 그린 탭바 아이콘.
 * native/web 둘 다 동작. focused 면 채워진 형태, 아니면 윤곽선만.
 */
import { StyleSheet, View } from "react-native";

interface Props {
  color: string;
  size?: number;
  focused?: boolean;
}

const STROKE = 1.8;

export function HomeIcon({ color, size = 22, focused }: Props) {
  const w = size;
  const roofH = size * 0.45;
  const bodyH = size * 0.42;
  const bodyW = size * 0.74;
  return (
    <View style={[styles.box, { width: w, height: size }]}>
      {/* 지붕: 위로 향한 삼각형 (border 트릭) */}
      <View
        style={{
          width: 0,
          height: 0,
          borderLeftWidth: w / 2,
          borderRightWidth: w / 2,
          borderBottomWidth: roofH,
          borderLeftColor: "transparent",
          borderRightColor: "transparent",
          borderBottomColor: color,
          marginBottom: -1,
        }}
      />
      {/* 본체 */}
      <View
        style={{
          width: bodyW,
          height: bodyH,
          backgroundColor: focused ? color : "transparent",
          borderColor: color,
          borderWidth: focused ? 0 : STROKE,
          borderTopWidth: 0,
        }}
      />
    </View>
  );
}

export function PeopleIcon({ color, size = 22, focused }: Props) {
  const headSize = size * 0.32;
  const bodyH = size * 0.34;
  const bodyW = size * 0.6;
  return (
    <View style={[styles.box, { width: size, height: size }]}>
      {/* 머리: 원 */}
      <View
        style={{
          width: headSize,
          height: headSize,
          borderRadius: headSize / 2,
          backgroundColor: focused ? color : "transparent",
          borderColor: color,
          borderWidth: focused ? 0 : STROKE,
          marginBottom: 2,
        }}
      />
      {/* 어깨: 위쪽이 둥근 사각형 (사람 윤곽) */}
      <View
        style={{
          width: bodyW,
          height: bodyH,
          borderTopLeftRadius: bodyW / 2,
          borderTopRightRadius: bodyW / 2,
          backgroundColor: focused ? color : "transparent",
          borderColor: color,
          borderWidth: focused ? 0 : STROKE,
          borderBottomWidth: 0,
        }}
      />
    </View>
  );
}

export function TrophyIcon({ color, size = 22, focused }: Props) {
  const cupW = size * 0.55;
  const cupH = size * 0.55;
  const handleW = size * 0.18;
  const handleH = size * 0.32;
  const stemW = size * 0.18;
  const stemH = size * 0.12;
  const baseW = size * 0.7;
  const baseH = size * 0.08;
  return (
    <View style={[styles.box, { width: size, height: size }]}>
      {/* 컵 + 양쪽 손잡이 */}
      <View style={{ flexDirection: "row", alignItems: "flex-start" }}>
        <View
          style={{
            width: handleW,
            height: handleH,
            borderColor: color,
            borderWidth: STROKE,
            borderRightWidth: 0,
            borderTopLeftRadius: handleW,
            borderBottomLeftRadius: handleW,
            marginTop: 1,
            marginRight: -1,
          }}
        />
        <View
          style={{
            width: cupW,
            height: cupH,
            backgroundColor: focused ? color : "transparent",
            borderColor: color,
            borderWidth: focused ? 0 : STROKE,
            borderBottomLeftRadius: cupW * 0.5,
            borderBottomRightRadius: cupW * 0.5,
            borderTopLeftRadius: 2,
            borderTopRightRadius: 2,
          }}
        />
        <View
          style={{
            width: handleW,
            height: handleH,
            borderColor: color,
            borderWidth: STROKE,
            borderLeftWidth: 0,
            borderTopRightRadius: handleW,
            borderBottomRightRadius: handleW,
            marginTop: 1,
            marginLeft: -1,
          }}
        />
      </View>
      {/* 줄기 */}
      <View
        style={{
          width: stemW,
          height: stemH,
          backgroundColor: color,
          marginTop: 1,
        }}
      />
      {/* 받침 */}
      <View
        style={{
          width: baseW,
          height: baseH,
          backgroundColor: color,
          borderRadius: 1,
          marginTop: 1,
        }}
      />
    </View>
  );
}

/**
 * 비디오/플레이어 아이콘 — 둥근 사각형 + 가운데 ▶ 삼각형.
 * 하이라이트 탭 (YouTube 영상 모음) 용.
 */
export function VideoIcon({ color, size = 22, focused }: Props) {
  const frameW = size * 0.95;
  const frameH = size * 0.72;
  const triSize = size * 0.32;
  return (
    <View style={[styles.box, { width: size, height: size }]}>
      {/* 프레임 (둥근 사각형) */}
      <View
        style={{
          width: frameW,
          height: frameH,
          borderRadius: 4,
          backgroundColor: focused ? color : "transparent",
          borderColor: color,
          borderWidth: focused ? 0 : STROKE,
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        {/* 가운데 ▶ — focused 면 흰색, 아니면 라인색 */}
        <View
          style={{
            width: 0,
            height: 0,
            borderLeftWidth: triSize,
            borderTopWidth: triSize * 0.6,
            borderBottomWidth: triSize * 0.6,
            borderTopColor: "transparent",
            borderBottomColor: "transparent",
            borderLeftColor: focused ? "#FFFFFF" : color,
            marginLeft: triSize * 0.3,
          }}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  box: { alignItems: "center", justifyContent: "center" },
});
