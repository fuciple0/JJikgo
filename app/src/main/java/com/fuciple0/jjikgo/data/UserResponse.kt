package com.fuciple0.jjikgo.data

// 상위 클래스
data class UserResponse(
    val nickname_user: String,  // 사용자 닉네임
    val level_user: String,     // 사용자 레벨
    val profileimg_user: String, // 사용자 프로필 이미지
    val total_score: Int,        // 전체 점수 합계
    val today_total_score: Int,  // 오늘의 점수 합계
    //val score_details: List<ScoreDetail> // 점수 상세 내역 리스트
)

// 중첩된 클래스 (score_details 배열 항목)
//data class ScoreDetail(
//    val date_score: String,  // 날짜 및 시간
//    val tag_score: String,   // 태그 (like, follow 등)
//    val num_score: Int       // 점수
//)
