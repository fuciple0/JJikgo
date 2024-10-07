package com.fuciple0.jjikgo.network

import com.fuciple0.jjikgo.data.KakoSearchPlaceResponse
import com.fuciple0.jjikgo.data.LoginResponse
import com.fuciple0.jjikgo.data.MemoResponse
import com.fuciple0.jjikgo.data.NaverUserInfoResponse
import com.fuciple0.jjikgo.data.RegisterResponse
import com.fuciple0.jjikgo.data.SharedMemoData
import com.fuciple0.jjikgo.data.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
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

    @Headers("Authorization: KakaoAK 63eca92fa882ec93e04b7c7dd8f4641b")
    @GET("/v2/local/search/keyword.json?sort=distance")
    fun searchPlacesFromServer(@Query("query") query:String,@Query("x") longitude:String,@Query("y") latitude:String) : Call<String>

    @Headers("Authorization: KakaoAK 63eca92fa882ec93e04b7c7dd8f4641b")
    @GET("/v2/local/search/keyword.json?sort=distance")
    fun searchPlacesFromServer2(@Query("query") query:String,@Query("x") longitude:String,@Query("y") latitude:String) : Call<KakoSearchPlaceResponse>





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

    // 기존 메모 업데이트
    @Multipart
    @POST("Jjikgo/update_memo.php")
    fun updateMemo(
        @PartMap dataPart: Map<String, String>,
        @Part filePart: MultipartBody.Part? // 이미지 파일이 있을 때만 추가
    ): Call<String>

    @DELETE("Jjikgo/delete_memo.php")
    fun deleteMemo(@Query("id_memo") idMemo: Int): Call<String>


    @GET("Jjikgo/get_shared_memo_data.php")
    fun getSharedMemoData(): Call<List<SharedMemoData>>


    // 범위 내의 메모를 가져오는 메서드
    @GET("Jjikgo/getMemosInBounds.php")
    fun getMemosInBounds(
        @Query("centerLat") centerLat: Double,
        @Query("centerLng") centerLng: Double,
        @Query("swLat") swLat: Double,  // 남서쪽 경계의 위도
        @Query("swLng") swLng: Double,  // 남서쪽 경계의 경도
        @Query("neLat") neLat: Double,  // 북동쪽 경계의 위도
        @Query("neLng") neLng: Double   // 북동쪽 경계의 경도
    ): Call<List<MemoResponse>>

    // 페이지 번호와 페이지 크기를 기반으로 최신 메모를 가져오는 API
    @GET("Jjikgo/getMemosSortedByDate.php")
    fun getMemosSortedByDate(
        @Query("limit") limit: Int,  // 한 번에 불러올 메모 수
        @Query("page") page: Int     // 페이지 번호
    ): Call<List<MemoResponse>>
}