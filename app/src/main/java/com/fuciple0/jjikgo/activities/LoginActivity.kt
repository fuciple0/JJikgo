package com.fuciple0.jjikgo.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.NaverUserInfoResponse
import com.fuciple0.jjikgo.data.RegisterResponse
import com.fuciple0.jjikgo.databinding.ActivityLoginBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

        private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
        private lateinit var googleSignInClient: GoogleSignInClient

        private companion object {
            private const val RC_SIGN_IN = 9001
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(binding.root)

            enableEdgeToEdge()
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            // Google Sign-In 설정
            configureGoogleSignIn()

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

            binding.btnLoginKakao.setOnClickListener { loginKaKao() } // 카카오 로그인 버튼
            binding.btnLoginNaver.setOnClickListener { loginNaver() } // 네이버 로그인 버튼
            binding.btnLoginGoogle.setOnClickListener { googleSignIn() } // 구글 로그인 버튼

        }

        private fun configureGoogleSignIn() {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)
        }
        private fun googleSignIn() {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == RC_SIGN_IN) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }

        private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
            try {
                val account = completedTask.getResult(ApiException::class.java)
                val email: String = account.email ?: ""
                val nickname: String = account.displayName ?: ""
                val profileImageBytes = getDefaultProfileImage() // 기본 이미지

                checkEmailAndUpload(nickname, email, "google", profileImageBytes)

            } catch (e: ApiException) {
                Log.w("Google Sign-In", "signInResult:failed code=" + e.statusCode)
                Toast.makeText(this, "구글 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }

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
                        val email:String = userInfo?.response?.email ?: ""
                        val nickname: String = userInfo?.response?.nickname ?: ""
                        // val profile_image: String = userInfo?.response?.profile_image ?: ""
                        val profileImageBytes = getDefaultProfileImage() // 기본 이미지

                        checkEmailAndUpload(nickname, email, "naver", profileImageBytes)
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
                //Toast.makeText(this, "카카오계정으로 로그인 성공", Toast.LENGTH_SHORT).show()
                // 사용자 정보 요청
                UserApiClient.instance.me { user, error ->
                    if (user != null) {
                        val id: String = user.id.toString()
                        val nickname: String = user.kakaoAccount?.profile?.nickname ?: ""
                        val email: String = user.kakaoAccount?.email?: "${nickname}@kakao.com"    // 앞에가 null이면 뒤에꺼 ""
                        //val profileImg:String?= user.kakaoAccount?.profile?.profileImageUrl
                        // 프로필 이미지 대신 기본 이미지 사용
                        val profileImageBytes = getDefaultProfileImage()

                        checkEmailAndUpload(nickname, email, "kakao", profileImageBytes)
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

    // 서버에 등록된 이메일이 있는지 확인한 후 데이터 업로드
    private fun checkEmailAndUpload(nickname: String, email: String, provider: String, profileImageBytes: ByteArray?) {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val callCheckEmail = retrofitService.checkEmailExists(email)
        callCheckEmail.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                val emailExists = response.body() ?: false
                if (!emailExists) {
                    uploadToServer(nickname, email, provider, profileImageBytes)
                } else {
                    Toast.makeText(this@LoginActivity, "이미 존재하는 이메일입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "중복 확인 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 사용자 정보를 서버에 업로드하는 메소드
    private fun uploadToServer(nickname: String, email: String, provider: String, profileImageBytes: ByteArray?) {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val dataPart: MutableMap<String, String> = mutableMapOf(
            "nickname_user" to nickname,
            "email_user" to email,
            "pw_user" to provider,
            "level_user" to "1"
        )

        val filePart: MultipartBody.Part? = profileImageBytes?.let {
            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), it)
            MultipartBody.Part.createFormData("profileimg_user", "profile_image.png", requestBody)
        }

        val call = retrofitService.registerUser(dataPart, filePart)
        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse?.success == true) {
                        val emailIndex = registerResponse.emailIndex ?: -1
                        saveLoginInfoToPreferences(nickname, email, emailIndex)
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, registerResponse?.message ?: "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "서버 저장 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    //SharedPreferences에 로그인 정보 저장
    private fun saveLoginInfoToPreferences(nickname: String, email: String, emailIndex: Int) {
        val sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("nickname", nickname)
        editor.putString("email", email)
        editor.putInt("email_index", emailIndex)
        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }


    // drawable의 기본 이미지를 ByteArray로 변환하는 함수
    private fun getDefaultProfileImage(): ByteArray {
        val drawable = resources.getDrawable(R.drawable.user_profile, null)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}


