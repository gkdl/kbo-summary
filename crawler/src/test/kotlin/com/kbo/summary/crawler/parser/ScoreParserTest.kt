package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.kbo.summary.core.exception.CrawlerException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ScoreParserTest {

    private val parser = ScoreParser(ObjectMapper())

    @Test
    fun `GetScoreBoardScroll 응답을 GameScoreDto로 파싱한다`() {
        val score = parser.parseScore(loadFixture("scoreResponse.json"))

        assertThat(score.gameId).isEqualTo("20260516HHKT0")
        assertThat(score.innings).hasSize(3)
        assertThat(score.awayR).isEqualTo(4)
        assertThat(score.awayH).isEqualTo(9)
        assertThat(score.awayE).isEqualTo(1)
        assertThat(score.awayB).isEqualTo(3)
        assertThat(score.homeR).isEqualTo(2)
        assertThat(score.homeH).isEqualTo(7)
        assertThat(score.homeB).isEqualTo(2)
    }

    @Test
    fun `중첩 JSON table2 의 이닝별 득점을 매핑한다`() {
        val score = parser.parseScore(loadFixture("scoreResponse.json"))

        assertThat(score.innings[0].inning).isEqualTo(1)
        assertThat(score.innings[0].awayRuns).isEqualTo(3)
        assertThat(score.innings[1].homeRuns).isEqualTo(2)
        assertThat(score.innings[2].awayRuns).isEqualTo(1)
    }

    @Test
    fun `code가 100이 아니면 CrawlerException을 던진다`() {
        assertThatThrownBy { parser.parseScore("{}") }
            .isInstanceOf(CrawlerException::class.java)
    }
}
