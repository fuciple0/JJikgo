package com.fuciple0.jjikgo.network

import androidx.appcompat.app.AlertDialog
import com.fuciple0.jjikgo.data.NaverUserInfoResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface RetrofitService {

    // 네이버 아이디 로그인 : 사용자 프로필 정보 OPEN API를 요청하는 작업 명세서.
    // 응답 결과를 json을 NaverUserInfoResponse 데이터 클래스 객체로 파싱하여 받아오기.
    //    @GET("/v1/nid/me")
    //    fun getNaverUserInfo(@Header("Authorization") authorization:String): Call<String> // 일단 String으로 값을 뽑아본다.
    //
    //    call.enqueue((object : Callback<String> {
    //        override fun onResponse(p0: Call<String>, p1: Response<String>) {
    //            val s = p1.body()
    //            AlertDialog.Builder(this@LoginActivity).setMessage("${s}").create().show()
    //        }

    @GET("/v1/nid/me")
    fun getNaverUserInfo(@Header("Authorization") authorization:String): Call<NaverUserInfoResponse>

    // 문자열과 이미지 파일을 함께 POST 방식으로 서버로 전송
    @Multipart
    @POST("/Jjikgo/register_user.php")  // php 파일 URL
    fun postDataToServer(
        @PartMap dataPart: Map<String, String>,  // 문자열 데이터 전송
        @Part filePart: MultipartBody.Part?      // 이미지 파일 전송
    ): Call<String> // 서버로부터 응답을 String으로 받음



}