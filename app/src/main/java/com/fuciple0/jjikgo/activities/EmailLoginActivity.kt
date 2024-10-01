package com.fuciple0.jjikgo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.LoginResponse
import com.fuciple0.jjikgo.databinding.ActivityEmailLoginBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmailLoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityEmailLoginBinding.inflate(layoutInflater) }
    var nickname: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 전달받은 이메일을 로그인 필드에 미리 입력
        val emailFromIntent = intent.getStringExtra("email")
        nickname = intent.getStringExtra("nickname")
        if (emailFromIntent != null) {
            binding.inputEmail.editText?.setText(emailFromIntent)
        }

        // 뒤로가기 버튼 클릭 반응
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 로그인 버튼 반응하기
        binding.btn.setOnClickListener { clickLogin() }

    }

    private fun clickLogin() {
        val email = binding.inputEmail.editText?.text.toString().trim()
        val password = binding.inputPw.editText?.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            if (isValidEmail(email)) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "모든 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 로그인 API 호출
    private fun loginUser(email: String, password: String) {

        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val call = retrofitService.loginUser(email, password)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                // 서버 응답 로그 출력
                Log.d("EmailLoginActivity9", "Server Response: ${response.body()}")

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.success == true) {
                        // 로그인 성공 처리 (MainActivity로 이동)
                        Toast.makeText(this@EmailLoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()


                        // 닉네임과 email_index를 받아서 SharedPreferences에 저장
                        val userNickname = nickname ?: loginResponse.nickname
                        val emailIndex = loginResponse.emailIndex ?: -1
                        if (userNickname != null && emailIndex != -1) {
                            saveLoginInfoToPreferences(userNickname, email, emailIndex)
                        }

                        val intent = Intent(this@EmailLoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        // 로그인 실패 시
                        Toast.makeText(this@EmailLoginActivity, loginResponse?.message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                        Log.e("EmailLoginActivity9", "Login failed: ${loginResponse?.message}")
                    }
                } else {
                    Toast.makeText(this@EmailLoginActivity, "서버 응답 실패", Toast.LENGTH_SHORT).show()
                    Log.e("EmailLoginActivity9", "Server Response Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@EmailLoginActivity, "로그인 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("EmailLoginActivity9", "Login API call failed: ${t.message}", t)
            }
        })
    }

    // 이메일 형식 유효성 검사
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // SharedPreferences에 로그인 정보 저장
    private fun saveLoginInfoToPreferences(nickname: String, email: String, emailIndex: Int) {
        val sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("nickname", nickname)
        editor.putString("email", email)
        editor.putInt("email_index", emailIndex)  // email_index 추가
        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }
}
