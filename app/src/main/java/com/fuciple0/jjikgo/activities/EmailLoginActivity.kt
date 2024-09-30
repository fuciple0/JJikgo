package com.fuciple0.jjikgo.activities

import android.content.Intent
import android.os.Bundle
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
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 뒤로가기 버튼 클릭 반응
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 로그인 버튼 반응하기
        binding.btn.setOnClickListener { clickLogin() }

        setContentView(binding.root)


    }

    private fun clickLogin() {
//        val email = binding.inputEmail.editText?.text.toString().trim()
//        val password = binding.inputPw.editText?.text.toString().trim()
//
//        // 사용자 정보를 데이터베이스에서 확인
//        val user = dbHelper.getUserByEmailAndPassword(email, password)
//
//        if (user != null) {
//            // 세션에 사용자 ID 저장
//            dbHelper.saveSession(user.id)
//
//            Toast.makeText(this, "로그인 완료", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, MainActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        } else {
//            Toast.makeText(this, "이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
//        }
   }
}