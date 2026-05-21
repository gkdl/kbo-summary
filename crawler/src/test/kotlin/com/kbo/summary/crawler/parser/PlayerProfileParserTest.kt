package com.kbo.summary.crawler.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerProfileParserTest {

    private val parser = PlayerProfileParser()

    @Test
    fun `타자 프로필 HTML의 기본 정보를 파싱한다`() {
        val profile = parser.parseHitterProfile(loadFixture("playerProfileHitter.html"))

        assertThat(profile.playerId).isEqualTo("60001")
        assertThat(profile.name).isEqualTo("홍길동")
        assertThat(profile.teamCode).isEqualTo("LG")
        assertThat(profile.position).isEqualTo("내야수")
        assertThat(profile.backNumber).isEqualTo("7")
        assertThat(profile.throws).isEqualTo("우투")
        assertThat(profile.bats).isEqualTo("좌타")
        assertThat(profile.birthDate).isEqualTo("1995-03-15")
        assertThat(profile.height).isEqualTo(183)
        assertThat(profile.weight).isEqualTo(88)
        assertThat(profile.school).isEqualTo("서울고")
        assertThat(profile.debutYear).isEqualTo(2014)
    }

    @Test
    fun `통산기록 테이블을 파싱한다`() {
        val profile = parser.parseHitterProfile(loadFixture("playerProfileHitter.html"))

        assertThat(profile.careerStats["타율"]).isEqualTo("0.298")
        assertThat(profile.careerStats["홈런"]).isEqualTo("180")
    }

    @Test
    fun `투수 프로필 HTML을 파싱한다`() {
        val profile = parser.parsePitcherProfile(loadFixture("playerProfilePitcher.html"))

        assertThat(profile.name).isEqualTo("이영호")
        assertThat(profile.teamCode).isEqualTo("SS")
        assertThat(profile.position).isEqualTo("투수")
        assertThat(profile.height).isEqualTo(187)
        assertThat(profile.debutYear).isEqualTo(2011)
    }
}
