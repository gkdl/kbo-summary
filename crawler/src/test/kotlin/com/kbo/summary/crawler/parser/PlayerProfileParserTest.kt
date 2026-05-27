package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerProfileParserTest {

    private val parser = PlayerProfileParser()

    @Test
    fun `실제 KBO 타자 페이지 — 박성한(SSG) 프로필을 ID selector 로 추출한다`() {
        val profile = parser.parseHitterProfile(loadFixture("playerProfileHitter.html"))

        // 회귀 방지: 옛 title fallback 이었던 "타자" 를 절대 이름으로 쓰면 안 된다
        assertThat(profile.name).isEqualTo("박성한")
        assertThat(profile.name).isNotEqualTo("타자")
        assertThat(profile.playerId).isEqualTo("67893")
        assertThat(profile.backNumber).isEqualTo("2")
        assertThat(profile.position).isEqualTo("내야수")
        assertThat(profile.throws).isEqualTo("우투")
        assertThat(profile.bats).isEqualTo("좌타")
        assertThat(profile.height).isEqualTo(180)
        assertThat(profile.weight).isEqualTo(77)
        assertThat(profile.birthDate).isEqualTo("1998년 03월 30일")
        assertThat(profile.debutYear).isEqualTo(2017)
    }

    @Test
    fun `실제 KBO 투수 페이지 — 후라도(삼성) 프로필 추출`() {
        val profile = parser.parsePitcherProfile(loadFixture("playerProfilePitcher.html"))

        assertThat(profile.name).isEqualTo("후라도")
        assertThat(profile.name).isNotEqualTo("투수")
        assertThat(profile.playerId).isEqualTo("53375")
        assertThat(profile.backNumber).isEqualTo("75")
        assertThat(profile.position).isEqualTo("투수")
        assertThat(profile.throws).isEqualTo("우투")
        assertThat(profile.bats).isEqualTo("우타")
        assertThat(profile.height).isEqualTo(188)
        assertThat(profile.weight).isEqualTo(109)
        // 2자리 연도 "23키움" → 2023 으로 정규화되는지
        assertThat(profile.debutYear).isEqualTo(2023)
    }
}
