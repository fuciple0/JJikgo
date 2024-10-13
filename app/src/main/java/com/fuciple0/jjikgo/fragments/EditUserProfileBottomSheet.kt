import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.activities.MainActivity
import com.fuciple0.jjikgo.databinding.FragmentEdituserprofileBottomsheetBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditUserProfileBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentEdituserprofileBottomsheetBinding? = null
    private val binding get() = _binding!!

    private var imgPath: String? = null // 프로필 이미지의 절대 경로 저장

    companion object {
        private const val ARG_PROFILE_IMAGE = "profile_image"
        private const val ARG_NICKNAME = "nickname"

        // 새로운 인스턴스를 생성할 때, 프로필 이미지 URI와 닉네임을 전달하는 메서드
        fun newInstance(profileImageUri: String?, nickname: String?): EditUserProfileBottomSheet {
            val fragment = EditUserProfileBottomSheet()
            val args = Bundle().apply {
                putString(ARG_PROFILE_IMAGE, profileImageUri)  // 프로필 이미지 URI 전달
                putString(ARG_NICKNAME, nickname)  // 닉네임 전달
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEdituserprofileBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileImageUri = arguments?.getString(ARG_PROFILE_IMAGE)
        val nickname = arguments?.getString(ARG_NICKNAME)

        // 프로필 이미지 설정 (절대 경로 사용)
        if (!profileImageUri.isNullOrEmpty()) {
            val imgUrl = "http://fuciple0.dothome.co.kr/Jjikgo/$profileImageUri"
            Glide.with(requireContext()).load(imgUrl).into(binding.userProfile)
        }
        // Retrofit 인스턴스 생성
        val retrofitService = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
            .create(RetrofitService::class.java)

        // 닉네임 설정
        binding.inputNickname.editText?.setText(nickname)

        // 프로필 이미지 선택 리스너
        binding.userProfile.setOnClickListener { clickSelect() }
        binding.userProfileFab.setOnClickListener { clickSelect() }

        // 저장 버튼 클릭 시
        binding.btnSignup.setOnClickListener {
            val newNickname = binding.inputNickname.editText?.text.toString()  // 닉네임 가져오기
            updateProfileToServer(retrofitService, newNickname, imgPath)  // 수정된 프로필 서버로 전송
        }

    }
    private fun updateProfileToServer(retrofitService: RetrofitService, nickname: String, imgPath: String?) {
        val emailIndex = G.emailIndex.toString()

        // 전송할 String 데이터들을 Map 컬렉션에 저장
        val dataPart: MutableMap<String, String> = mutableMapOf(
            "nickname" to nickname,
            "email_index" to emailIndex
        )

        // 이미지 파일을 선택한 경우에만 파일 포장
        val filePart: MultipartBody.Part? = imgPath?.let {
            val file = File(it)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profile_image", file.name, requestBody)
        }

        // Retrofit 서비스 호출
        val call = retrofitService.updateProfile(dataPart, filePart)
        Log.d("Retrofit", "Updating profile data: $dataPart")
        filePart?.let {
            Log.d("Retrofit", "Uploading image: ${filePart.body.contentType()}")
        }

        // 서버 응답 처리
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 서버 응답 성공 시
                    if (isAdded) {
                        Toast.makeText(requireContext(), "프로필이 업데이트되었습니다!", Toast.LENGTH_SHORT).show()
                        (context as MainActivity).binding.bnv.selectedItemId = R.id.bnv_menu_account
                        dismiss()  // 바텀시트 닫기
                    }
                } else {
                    // 서버 응답 실패 시
                    if (isAdded) {
                        Toast.makeText(requireContext(), "프로필 업데이트 실패: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 이미지 선택을 위한 클릭 이벤트 처리
    private fun clickSelect() {
        // Android 13 이상에서는 ACTION_PICK_IMAGES 사용, 그 이하에서는 ACTION_OPEN_DOCUMENT 사용
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(MediaStore.ACTION_PICK_IMAGES)
        } else {
            Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*")
        }
        resultLauncher.launch(intent)
    }

    // 선택된 이미지 처리
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val uri: Uri? = it.data?.data
        if (uri != null) {
            Glide.with(this).load(uri).into(binding.userProfile) // 선택된 이미지 표시
            imgPath = getRealPathFromUri(uri) // URI -> 절대 경로 변환 후 저장
        }
    }

    // URI에서 절대 경로를 가져오는 함수
    private fun getRealPathFromUri(uri: Uri): String? {
        var realPath: String? = null
        val cursor: Cursor? = requireContext().contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                if (idx != -1) {
                    realPath = cursor.getString(idx)
                }
            }
            cursor.close()
        }
        return realPath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
