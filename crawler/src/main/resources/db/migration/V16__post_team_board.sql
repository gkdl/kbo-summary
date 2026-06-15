-- 팀 게시판 모델로 전환: 카테고리 제거, 팀 코드를 게시판 식별자로 필수화.
-- (운영 미배포 단계라 기존 테스트 데이터의 NULL TEAM_CODE 는 임시 보정 후 NOT NULL 적용)
UPDATE TB_POST SET TEAM_CODE = 'LG' WHERE TEAM_CODE IS NULL;
ALTER TABLE TB_POST MODIFY (TEAM_CODE VARCHAR2(10) NOT NULL);

-- CATEGORY 컬럼 제거 (의존 인덱스 IX_POST_CATEGORY 는 컬럼과 함께 자동 삭제됨)
ALTER TABLE TB_POST DROP COLUMN CATEGORY;
