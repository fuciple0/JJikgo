package com.fuciple0.jjikgo.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    val binding by lazy { ActivitySignupBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 뒤로가기 버튼 클릭 반응
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 가입하기 버튼 클릭 반응
        binding.btn.setOnClickListener {

            Toast.makeText(this, "회원가입되었습니다.", Toast.LENGTH_SHORT).show()

            finish()
        }


    }
}