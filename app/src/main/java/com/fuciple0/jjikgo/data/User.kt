package com.fuciple0.jjikgo.data

data class User(
    val id: Int,
    val nickname: String,
    val profileImage: ByteArray? // 이미지가 없을 수도 있으니 nullable로 처리
)