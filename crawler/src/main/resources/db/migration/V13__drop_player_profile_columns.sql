-- 선수 프로필 정보(생년월일·신장·체중·출신교·입단년도·투타)는
-- 화면에서 더 이상 사용하지 않아 컬럼을 제거한다.
ALTER TABLE TB_PLAYER DROP (
    BATS,
    THROWS,
    BIRTH_DATE,
    HEIGHT,
    WEIGHT,
    SCHOOL,
    DEBUT_YEAR
);
