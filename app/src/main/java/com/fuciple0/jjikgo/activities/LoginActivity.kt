package com.fuciple0.jjikgo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.NaverUserInfoResponse
import com.fuciple0.jjikgo.databinding.ActivityLoginBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //둘러보기 글씨 클릭으로 Main 화면으로 이동
        binding.tvGo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // 회원가입 버튼 클릭
        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // 이메일 로그인 버튼
        binding.btnLoginEmail.setOnClickListener {
            startActivity(Intent(this, EmailLoginActivity::class.java))
        }

        // 카카오 로그인 버튼
        binding.btnLoginKakao.setOnClickListener { loginKaKao() }

        // 네이버 로그인 버튼
        binding.btnLoginNaver.setOnClickListener { loginNaver() }

    }//onCreate

    private fun loginNaver() {
        // 1. 네이버 개발자 사이트에서 앱 등록 및 라이브러리 추가
        // 2. 네이버 아이디 로그인(네아로) SDK 초기화
        NaverIdLoginSDK.initialize(this, "OLjoucgqFdPqX2hTFjG0", "Yh_OWOJA1y", "찍고")

        // 3. 로그인 작업 수행
        NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback {
            override fun onError(errorCode: Int, message: String) {
                Toast.makeText(this@LoginActivity, "에러 발생:${message}", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(httpStatus: Int, message: String) {
                Toast.makeText(this@LoginActivity, "로그인 실패:${message}", Toast.LENGTH_SHORT).show()
            }
            override fun onSuccess() {
                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                // 1차적으로 로그인 작업 완료.
                // 이제 회원 프로필 정보를 얻어와야 하는데,
                // 회원 프로필 정보는  REST API를 이용하여 요청하고 응답 받아야 한다. (회원 프로필 조회 API 명세 - 확인)
                val accessToken:String? = NaverIdLoginSDK.getAccessToken()

                // 토큰번호가 잘 오는지 확인
                Log.i("Token", "${accessToken}")

                // 레트로핏 작업을 위해서 사용자 정보 가져오기..(API 명세 - 확인)
                val retrofit = RetrofitHelper.getRetrofitInstance("https://openapi.naver.com")

                //RetrofitService 인터페이스 작성
                val retrofitService = retrofit.create(RetrofitService::class.java)
                val call = retrofitService.getNaverUserInfo("Bearer $accessToken")

                call.enqueue(object : Callback<NaverUserInfoResponse> {
                    override fun onResponse(
                        p0: Call<NaverUserInfoResponse>,
                        p1: Response<NaverUserInfoResponse>
                    ) {
                       //응답 객체 p1으로부터, 받은 결과를(json데이터)를 NaverUserInfoResponse로 분석한 결과 받기
                        val userInfo = p1.body()
                        val id: String = userInfo?.response?.id ?: ""
                        val email:String = userInfo?.response?.email ?: ""
                        val nickname: String = userInfo?.response?.nickname ?: ""
                        val profile_image: String = userInfo?.response?.profile_image ?: ""

//                        val message = """
//                            ID: $id
//                            Email: $email
//                            Nickname: $nickname
//                            Profile Image URL: $profile_image
//                        """.trimIndent()
//
//                        AlertDialog.Builder(this@LoginActivity)
//                            .setTitle("네이버 사용자 정보")
//                            .setMessage(message)
//                            .setPositiveButton("확인") { dialog, _ ->
//                                dialog.dismiss()
//                            }
//                            .create()
//                            .show()

                        // 로그인 했으니 메인 화면으로 이동
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()

                    }

                    override fun onFailure(p0: Call<NaverUserInfoResponse>, p1: Throwable) {
                        Toast.makeText(this@LoginActivity, "회원정보 불러오기 실패 : ${p1.message}", Toast.LENGTH_SHORT).show()
                    }
                })




            }
        })


    }

    private fun loginKaKao() {
        // 카카오 로그인 api 라이브러리 추가하기.

        // 로그인 조합 예제

        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Toast.makeText(this, "카카오계정으로 로그인 실패", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                Toast.makeText(this, "카카오계정으로 로그인 성공", Toast.LENGTH_SHORT).show()

                // 사용자 정보 요청
                UserApiClient.instance.me { user, error ->
                    if (user != null){
                        val id:String = user.id.toString()
                        val email:String = user.kakaoAccount?.email ?: ""    // 앞에가 null이면 뒤에꺼 ""
                        val nickname:String = user.kakaoAccount?.profile?.nickname ?: ""
                        val profileImg:String?= user.kakaoAccount?.profile?.profileImageUrl

                        Toast.makeText(this, "$nickname", Toast.LENGTH_SHORT).show()
                       // G.userAccount = UserAccount(id, email, "kakao")


                        // main 화면으로 이동
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }

            }
        }

// 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }  else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }


    }


}