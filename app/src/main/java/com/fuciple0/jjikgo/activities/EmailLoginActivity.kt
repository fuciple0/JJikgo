package com.fuciple0.jjikgo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.databinding.ActivityEmailLoginBinding

class EmailLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val binding by lazy { ActivityEmailLoginBinding.inflate(layoutInflater) }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 뒤로가기 버튼 클릭 반응
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 로그인 버튼 반응하기
        binding.btn.setOnClickListener { clickLogin() }

    }

    private fun clickLogin() {

        Toast.makeText(this, "로그인 완료", Toast.LENGTH_SHORT).show()
        
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}