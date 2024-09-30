package com.fuciple0.jjikgo.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.MemoDatabaseHelper
import com.fuciple0.jjikgo.databinding.ActivityEmailLoginBinding

class EmailLoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityEmailLoginBinding.inflate(layoutInflater) }
    private lateinit var dbHelper: MemoDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = MemoDatabaseHelper(this)

        // 뒤로가기 버튼 클릭 반응
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 로그인 버튼 반응하기
        binding.btn.setOnClickListener { clickLogin() }

    }

    private fun clickLogin() {
        val email = binding.inputEmail.editText?.text.toString().trim()
        val password = binding.inputPw.editText?.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            // 이메일과 비밀번호로 사용자 조회
            val user = dbHelper.getUserByEmailAndPassword(email, password)
            if (user != null) {
                // 세션에 사용자 ID 저장
                dbHelper.saveSession(user.id)

                Toast.makeText(this, "로그인 완료", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this, "이메일 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "모든 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 이메일 형식 유효성 검사
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
