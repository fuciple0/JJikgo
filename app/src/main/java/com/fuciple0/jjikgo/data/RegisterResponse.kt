package com.fuciple0.jjikgo.data

data class RegisterResponse(
    val success: Boolean,  // 회원가입 성공 여부
    val emailIndex: Int,   // 서버에서 받은 email_index 값
    val message: String?   // 실패 시 에러 메시지
)
