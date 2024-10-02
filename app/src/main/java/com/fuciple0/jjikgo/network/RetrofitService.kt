package com.fuciple0.jjikgo.network

import com.fuciple0.jjikgo.data.LoginResponse
import com.fuciple0.jjikgo.data.MemoResponse
import com.fuciple0.jjikgo.data.NaverUserInfoResponse
import com.fuciple0.jjikgo.data.RegisterResponse
import com.fuciple0.jjikgo.data.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query


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


    // 이메일 중복 확인 API
    @GET("Jjikgo/checkEmail.php")
    fun checkEmailExists(
        @Query("email_user") email: String
    ): Call<Boolean>

    @FormUrlEncoded
    @POST("Jjikgo/login.php")
    fun loginUser(
        @Field("email_user") email: String,
        @Field("pw_user") password: String
    ): Call<LoginResponse>


    @GET("Jjikgo/getUserInfo.php")
    fun getUserInfo(@Query("emailIndex") userId: Int): Call<UserResponse>


    // 회원가입 API 호출 (문자열과 이미지 파일을 함께 전송)
    @Multipart
    @POST("Jjikgo/register_user.php")  // php 파일 URL
    fun registerUser(
        @PartMap data: Map<String, String>,  // 문자열 데이터를 보낼 때 사용
        @Part profileImage: MultipartBody.Part?  // 선택적 프로필 이미지
    ): Call<RegisterResponse>


    @Multipart
    @POST("Jjikgo/upload_memo.php")
    fun uploadMemo(
        @PartMap dataPart: Map<String, String>,
        @Part filePart: MultipartBody.Part?
    ): Call<String>

    @GET("Jjikgo/get_memos.php")
    fun getMemos(
        @Query("email_index") emailIndex: Int?,
        @Query("share_memo") shareMemo: Int
    ): Call<List<MemoResponse>>


}