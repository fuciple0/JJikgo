package com.fuciple0.jjikgo.data

data class UserResponse(
    val nickname: String,
    val profileImage: ByteArray?,  // 이미지가 null일 수 있음
    val level: String  // 서버에서 받은 레벨 정보
)