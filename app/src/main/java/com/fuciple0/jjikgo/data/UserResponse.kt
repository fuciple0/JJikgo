package com.fuciple0.jjikgo.data

data class UserResponse(
    val nickname: String,
    val profileImage: String,  
    val level: Int  // 서버에서 받은 레벨 정보
)