package com.fuciple0.jjikgo.data


// 전송된 제이슨 파일을 보고, 맞춰서 데이터 클래스를 작성한다.

data class NaverUserInfoResponse(val resultcode:String, val message:String, val response:UserInfo)


data class UserInfo(val id:String, val nickname:String, val profile_image:String, val email:String)
