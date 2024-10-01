package com.fuciple0.jjikgo.activities

import android.app.Activity
import android.content.Context
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
import com.fuciple0.jjikgo.data.RegisterResponse
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
                // 이메일 중복 확인 후 회원가입 진행
                checkEmailAndSignup(nickname, email, password)
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

    // 이메일 중복 확인 후 회원가입 진행
    private fun checkEmailAndSignup(nickname: String, email: String, password: String) {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        // 이메일 중복 확인 API 호출
        val callCheckEmail = retrofitService.checkEmailExists(email)
        callCheckEmail.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                val emailExists = response.body() ?: false

                if (emailExists) {
                    // 이메일이 이미 존재하는 경우
                    Toast.makeText(this@SignupActivity, "이미 존재하는 이메일입니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignupActivity, EmailLoginActivity::class.java)
                    intent.putExtra("email", email)
                    intent.putExtra("nickname", nickname)
                    startActivity(intent)
                    finish() // SignupActivity 종료
                } else {
                    // 이메일이 중복되지 않으면 회원가입 진행
                    uploadToServerAndFetchEmailIndex(nickname, email, password)
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Toast.makeText(this@SignupActivity, "이메일 확인 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 회원가입 후 서버에서 email_index 값을 받아오는 함수
    private fun uploadToServerAndFetchEmailIndex(nickname: String, email: String, password: String) {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val dataPart: MutableMap<String, String> = mutableMapOf()
        dataPart["nickname_user"] = nickname
        dataPart["email_user"] = email
        dataPart["pw_user"] = password
        dataPart["level_user"] = "1"  // 기본값 1 설정

        // 이미지 파일을 MultipartBody.Part로 포장
        val filePart: MultipartBody.Part? = imgPath?.let {
            val file = File(it)
            val requestBody = RequestBody.create("image/*".toMediaType(), file)
            MultipartBody.Part.createFormData("profileimg_user", file.name, requestBody)
        }

        val call = retrofitService.registerUser(dataPart, filePart)
        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()

                    // 서버에서 성공적으로 email_index를 받은 경우
                    if (registerResponse?.success == true) {
                        val emailIndex = registerResponse.emailIndex  // 서버에서 받은 email_index

                        // email_index를 SharedPreferences에 저장
                        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putInt("email_index", emailIndex)  // email_index 저장
                        editor.putBoolean("is_logged_in", true)  // 로그인 상태 저장
                        editor.apply()

                        // 사용자 정보도 SharedPreferences에 저장
                        saveLoginInfoToPreferences(nickname, email)

                        // MainActivity로 이동
                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@SignupActivity, registerResponse?.message ?: "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@SignupActivity, "서버 통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // SharedPreferences에 로그인 정보 저장
    private fun saveLoginInfoToPreferences(nickname: String, email: String) {
        val sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("nickname", nickname)
        editor.putString("email", email)
        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }
}
