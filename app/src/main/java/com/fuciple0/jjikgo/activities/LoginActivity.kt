package com.fuciple0.jjikgo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

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