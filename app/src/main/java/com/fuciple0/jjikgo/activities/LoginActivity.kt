package com.fuciple0.jjikgo.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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


    }
}