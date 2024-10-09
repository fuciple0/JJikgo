package com.fuciple0.jjikgo.data

data class SharedMemoData(
    val id_memo: Int,              // 메모 ID
    val addr_memo: String,         // 메모 주소
    val score_memo: Int,
    val img_memo: String?,         // 메모 이미지 경로
    val text_memo: String,         // 메모 텍스트
    val x_memo: Double,            // 메모 위도 37.417154222129184   latitude
    val y_memo: Double,            // 메모 경도 -122.07820930926918  longitude
    val date_memo: String,         // 메모 작성 날짜, //    "share_memo":"1", 제외
    val target_email_index: Int,   // 메모 작성자의 email_index
    val nickname_user: String,     // 사용자 닉네임
    val level_user: Int,           // 사용자 레벨
    val profileimg_user: String?,  // 사용자 프로필 이미지
    val user_email_index: Int,       // 로그인한 사용자의 email_index

    val memo_count: Int,           // 메모 개수
    val score_average: Float,      // 메모 점수 평균
    val followerCount: Int,        // 팔로워 수
    val likeCount: Int,            // 좋아요 수

    var isBookmarked: Int,   // 북마크 상태
    var isFollowing: Int,    // 팔로우 상태
    var isLiked: Int      // 좋아요 상태


)
