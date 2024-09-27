package com.fuciple0.jjikgo.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.MemoDatabaseHelper
import com.fuciple0.jjikgo.databinding.ActivitySignupBinding
import java.io.ByteArrayOutputStream

class SignupActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySignupBinding.inflate(layoutInflater) }
    private lateinit var dbHelper: MemoDatabaseHelper
    private var selectedImageUri: Uri? = null

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

        // 프로필 이미지 선택 클릭 반응
        binding.userProfileFab.setOnClickListener { clickSelect() }
        binding.userProfile.setOnClickListener { clickSelect() }

// 가입하기 버튼 클릭 반응
        binding.btnSignup.setOnClickListener {
            val nickname = binding.inputNickname.editText?.text.toString()
            val email = binding.inputEmail.editText?.text.toString()
            val password = binding.inputPw.editText?.text.toString()

            if (nickname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                val profileImageBlob = selectedImageUri?.let { getImageBlob(it) }
                dbHelper.insertUser(nickname, email, password, profileImageBlob)

                Toast.makeText(this, "회원가입되었습니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun clickSelect() {
        // 사진 or 갤러리 앱을 실행해서 업로드할 사진을 선택
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Intent(MediaStore.ACTION_PICK_IMAGES)
        else
            Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*")
        resultLauncher.launch(intent)
    }

    // 결과를 처리하는 대행사
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                selectedImageUri = uri // 선택된 이미지 URI 저장
                Glide.with(this).load(uri).into(binding.userProfile) // 이미지뷰에 선택된 이미지 표시
                binding.userProfileFab.visibility = View.GONE // FAB 숨김
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.userProfile.setImageURI(selectedImageUri)
            binding.userProfileFab.visibility = View.GONE // FAB 숨김
        }
    }


    private fun getImageBlob(uri: Uri): ByteArray? {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            return ByteArrayOutputStream().apply {
                inputStream.copyTo(this)
            }.toByteArray()
        }
        return null
    }

    companion object {
        private const val IMAGE_PICK_REQUEST_CODE = 1001
    }

}