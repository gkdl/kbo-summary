package com.kbo.summary.core.exception

class GameNotFoundException(gameId: String) :
    BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "경기를 찾을 수 없습니다: $gameId")

class TeamNotFoundException(teamCode: String) :
    BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "팀을 찾을 수 없습니다: $teamCode")

class PlayerNotFoundException(playerId: String) :
    BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "선수를 찾을 수 없습니다: $playerId")

class SearchEmptyException(keyword: String) :
    BusinessException(ErrorCode.INVALID_INPUT, "검색어는 2글자 이상이어야 합니다: '$keyword'")

class CrawlerException(message: String, cause: Throwable? = null) :
    BusinessException(ErrorCode.CRAWLING_FAILED, message, cause)

class ParseException(message: String, cause: Throwable? = null) :
    BusinessException(ErrorCode.PARSE_FAILED, message, cause)

class SummaryException(message: String, cause: Throwable? = null) :
    BusinessException(ErrorCode.SUMMARY_FAILED, message, cause)
