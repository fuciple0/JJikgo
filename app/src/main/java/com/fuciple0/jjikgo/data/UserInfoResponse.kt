package com.fuciple0.jjikgo.data

data class UserInfoResponse(
    val status: String,
    val nickname: String?,
    val level: String?,
    val profile_image: String?,
    val total_score: Int?,  // 전체 점수
    val today_total_score: Int?  // 오늘의 점수
)
