package com.fuciple0.jjikgo.data

data class UserInfoResponse(
    val status: String,
    val nickname: String?,
    val level: String?,
    val profile_image: String?,
    val total_score: Int?,  // 전체 점수
    val today_total_score: Int? , // 오늘의 점수
    val score_details: List<ScoreDetail>?  // 점수 상세 내역 배열
)

data class ScoreDetail(
    val date_score: String,  // 날짜
    val tag_score: String,   // 점수 태그 (like, follow 등)
    val num_score: Int       // 점수 값
)