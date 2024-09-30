package com.fuciple0.jjikgo.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import okhttp3.MediaType.Companion.toMediaType

class SignupActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySignupBinding.inflate(layoutInflater) }
    private lateinit var dbHelper: MemoDatabaseHelper
    private var selectedImageUri: Uri? = null
    private var imgPath: String? = null  // 이미지 절대경로를 저장하는 변수

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
            val nickname = binding.inputNickname.editText?.text.toString().trim()
            val email = binding.inputEmail.editText?.text.toString().trim()
            val password = binding.inputPw.editText?.text.toString().trim()

            if (nickname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password.length >= 6) {
                val profileImageBlob = selectedImageUri?.let { getImageBlob(it) }
                dbHelper.insertUser(nickname, email, password, profileImageBlob)

                // 세션에 사용자 ID 저장 (여기서는 1을 예시로 사용)
                dbHelper.saveSession(1) // 사용자 ID를 저장하는 메서드 추가 필요

                // 외부 서버에 데이터 업로드
                uploadToServer(nickname, email, password)


                Toast.makeText(this, "회원가입되었습니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "모든 내용를 채워주세요. 비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
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
                selectedImageUri = uri
                imgPath = getRealPathFromUri(uri)  // 절대 경로 가져오기
                Glide.with(this).load(uri).into(binding.userProfile)  // 이미지뷰에 표시
                binding.userProfileFab.visibility = View.GONE
            }
        }
    }

    // 절대 경로 가져오기
    private fun getRealPathFromUri(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                return it.getString(idx)
            }
        }
        return null
    }

    // 외부 서버로 데이터 업로드
    private fun uploadToServer(nickname: String, email: String, password: String) {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        // String 데이터들을 Map에 저장
        val dataPart: MutableMap<String, String> = mutableMapOf()
        dataPart["nickname_user"] = nickname
        dataPart["email_user"] = email
        dataPart["pw_user"] = password
        dataPart["level_user"] = "1"  // level_user 기본값은 1

        // 이미지 파일을 MultipartBody.Part로 포장
        val filePart: MultipartBody.Part? = imgPath?.let {
            val file = File(it)
            val requestBody = RequestBody.create("image/*".toMediaType(), file)  // 수정된 부분
            MultipartBody.Part.createFormData("profileimg_user", file.name, requestBody)
        }

        // 서버로 데이터 전송
        val call = retrofitService.postDataToServer(dataPart, filePart)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val responseBody = response.body()


                //Toast.makeText(this@SignupActivity, responseBody, Toast.LENGTH_SHORT).show()

                // 서버 응답 확인을 위한 로그 추가
                Log.d("SignupActivity9", "Response Body9: $responseBody")

                // 서버 응답이 성공했는지 확인
                if (responseBody != null) {
                    Toast.makeText(this@SignupActivity, responseBody, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SignupActivity, "Response body is null", Toast.LENGTH_SHORT).show()
                }


            // 업로드 완료 후 화면 전환
                startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                finish()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                // 로그로 에러 메시지 출력
                Log.e("SignupActivity", "Error:요기 ${t.message}", t)
                Toast.makeText(this@SignupActivity, "Error:여기 ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


    companion object {
        private const val IMAGE_PICK_REQUEST_CODE = 1001
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



}