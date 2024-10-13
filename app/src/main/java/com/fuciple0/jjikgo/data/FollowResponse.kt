package com.fuciple0.jjikgo.data

data class FollowResponse(
    val status: String,  // success 또는 error
    val message: String,  // 상태 메시지
    val userData: UserData? = null  // 검색된 사용자 정보 (없으면 null)
)

data class UserData(
    val emailIndex: Int,
    val nickname: String,
    val email: String
)