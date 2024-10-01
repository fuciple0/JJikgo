package com.fuciple0.jjikgo.data

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val emailIndex: Int?,  // 서버에서 받은 email_index 값
    val nickname: String?
)