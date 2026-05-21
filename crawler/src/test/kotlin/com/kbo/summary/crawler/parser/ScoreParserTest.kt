package com.kbo.summary.crawler.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.kbo.summary.core.exception.CrawlerException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ScoreParserTest {

    private val parser = ScoreParser(ObjectMapper())

    @Test
    fun `스코어보드 JSON을 GameScoreDto로 파싱한다`() {
        val score = parser.parseScore(loadFixture("scoreResponse.json"))

        assertThat(score.gameId).isEqualTo("20240601LGOB0")
        assertThat(score.innings).hasSize(9)
        assertThat(score.homeR).isEqualTo(4)
        assertThat(score.homeH).isEqualTo(9)
        assertThat(score.awayE).isEqualTo(1)
        assertThat(score.awayB).isEqualTo(2)
    }

    @Test
    fun `이닝별 득점을 순서대로 매핑한다`() {
        val score = parser.parseScore(loadFixture("scoreResponse.json"))

        assertThat(score.innings[0].inning).isEqualTo(1)
        assertThat(score.innings[0].awayRuns).isEqualTo(1)
        assertThat(score.innings[3].homeRuns).isEqualTo(2)
    }

    @Test
    fun `점수 데이터가 없으면 CrawlerException을 던진다`() {
        assertThatThrownBy { parser.parseScore("{}") }
            .isInstanceOf(CrawlerException::class.java)
    }
}
