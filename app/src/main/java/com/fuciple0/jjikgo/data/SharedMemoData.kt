package com.fuciple0.jjikgo.data

data class SharedMemoData(
    val nickname: String,
    val level: Int,
    val memoCount: Int,
    val scoreAverage: Float,
    val followerCount: Int,
    val likeCount: Int,
    val memoText: String,
    val memoImage: String?,
    val memoDate: String,
    val userProfile: String?,  // 사용자 프로필 이미지
    val addrMemo: String,      // 주소 데이터 추가
    val latitude: Double,      // 위도 추가
    val longitude: Double      // 경도 추가
)

