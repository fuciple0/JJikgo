package com.fuciple0.jjikgo.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.TooltipCompat
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.activities.MainActivity
import com.fuciple0.jjikgo.network.RetrofitService
import com.fuciple0.jjikgo.databinding.FragmentAddmemoBottomsheetBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naver.maps.geometry.LatLng
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale



class AddmemoFragment : BottomSheetDialogFragment() {


    // ViewBinding 객체 선언
    private var _binding: FragmentAddmemoBottomsheetBinding? = null
    private val binding get() = _binding!!

    // 멤버변수 - 업로드할 이미지의 절대 주소를 저장하는 문자열 참조변수
    var imgPath: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddmemoBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 데이터로 UI 채우기
        val idMemo = arguments?.getInt("id_memo", -1)  // id_memo 값이 있으면 받음, 없으면 -1
        val addressMemo = arguments?.getString("addressMemo")
        val rating = arguments?.getFloat("rating", 0f)
        val memoText = arguments?.getString("memoText")
        val imageUrl = arguments?.getString("imageUrl")
        val x = arguments?.getString("x")
        val y = arguments?.getString("y")

        binding.addressTv.text = addressMemo
        binding.ratingBar.rating = rating ?: 0f
        binding.memoEditText.setText(memoText)

        if (imageUrl.isNullOrEmpty()) {
            // 이미지가 없을 때 툴팁을 설정
            TooltipCompat.setTooltipText(binding.memoImage, "이미지를 추가해 보세요")
        } else {
            // 이미지가 있을 경우에만 Glide로 이미지 로드
            val fullImageUrl = "http://fuciple0.dothome.co.kr/Jjikgo/$imageUrl"
            Glide.with(this).load(fullImageUrl).into(binding.memoImage)
        }

        // Retrofit 인스턴스 생성
        val retrofitService = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
            .create(RetrofitService::class.java)

        // id_memo 값이 없으면 새로운 메모 추가, 있으면 업데이트
        binding.saveMemoButton1.setOnClickListener {
            if (idMemo == -1) {
                saveMemoToServer(retrofitService, x, y, false)
            } else {
                updateMemoToServer(retrofitService, idMemo.toString(), x, y, false)
            }
        }

        binding.saveMemoButton2.setOnClickListener {
            if (idMemo == -1) {
                saveMemoToServer(retrofitService, x, y, true)
            } else {
                updateMemoToServer(retrofitService, idMemo.toString(), x, y, true)
            }
        }

        // 이미지 선택 리스너
        binding.memoImage.setOnClickListener { clickSelect() }
    }

    private fun clickSelect() {
        // 사진 or 갤러리 앱을 실행해서 업로드할 사진을 선택하고자 함. - 결과를 받기 위한 액티비티 실행
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Intent(MediaStore.ACTION_PICK_IMAGES)
        else
            Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*")
        resultLauncher.launch(intent)
    }

    // 대행사
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val uri: Uri? = it.data?.data
        if (uri != null) {
            Glide.with(this).load(uri).into(binding.memoImage) // 이미지뷰에 선택된 이미지 표시
            imgPath = getRealPathFromUri(uri) // URI -> 절대 경로 변환 후 저장
        }
    }


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

//    private fun saveMemoToDatabase(x: String?, y: String?) {
//        val address = binding.addressTv.text.toString()
//        val rating = binding.ratingBar.rating
//        val memoText = binding.memoEditText.text.toString()
//
//        // 이미지 경로가 null일 경우 기본 이미지 설정
//        val imageBlob = imgPath?.let { getImageBlob(it) } ?: getDefaultImageBlob()
//
//
//        // 현재 날짜 및 시간 가져오기
//        val currentDateTime = getCurrentDateTime()
//
//        if (address.isNotEmpty() && memoText.isNotEmpty()) {
//            val id = dbHelper.insertMemo(address, rating, imageBlob, memoText, x, y, currentDateTime)
//            G.userlocation = LatLng(y!!.toDouble(), x!!.toDouble())
//
//            if (id != -1L) {
//                Toast.makeText(requireContext(), "메모가 저장되었습니다!", Toast.LENGTH_SHORT).show()
//                dismiss()
//                (context as MainActivity).binding.bnv.selectedItemId = R.id.bnv_menu_location
//            } else {
//                Toast.makeText(requireContext(), "저장 실패!", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(requireContext(), "모든 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
//        }
//    }

    // 이미지 파일을 BLOB 형태로 변환하는 함수
//    private fun getImageBlob(imagePath: String): ByteArray {
//        val file = File(imagePath)
//        return file.readBytes()
//    }

    // 기본 이미지의 BLOB 데이터 반환
//    private fun getDefaultImageBlob(): ByteArray {
//        val defaultImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
//        val stream = ByteArrayOutputStream()
//        defaultImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
//        return stream.toByteArray()
//    }

    // 현재 날짜 및 시간을 "yyyy-MM-dd HH:mm:ss" 형식으로 반환하는 함수
    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(System.currentTimeMillis())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveMemoToServer(retrofitService: RetrofitService, x: String?, y: String?, shareMemo: Boolean) {
        val address = binding.addressTv.text.toString()
        val rating = binding.ratingBar.rating.toString().ifEmpty { "0" }  // 빈 값일 경우 기본값 0을 설정
        val memoText = binding.memoEditText.text.toString()

        // SharedPreferences에서 emailIndex 값을 가져옴
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val emailIndex = sharedPreferences.getInt("email_index", -1).toString()

        if (emailIndex == "-1") {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 전송할 String 데이터들을 Map 컬렉션에 저장
        val dataPart: MutableMap<String, String> = mutableMapOf()
        dataPart["addr_memo"] = address
        dataPart["score_memo"] = rating  // rating의 기본값을 0으로 설정
        dataPart["text_memo"] = memoText
        dataPart["x_memo"] = x ?: ""
        dataPart["y_memo"] = y ?: ""
        dataPart["date_memo"] = getCurrentDateTime()
        dataPart["share_memo"] = if (shareMemo) "1" else "0"
        dataPart["email_index"] = emailIndex

        // 이미지 파일을 선택한 경우에만 파일 포장
        val filePart: MultipartBody.Part? = imgPath?.let {
            val file = File(it)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("img_memo", file.name, requestBody)
        }

        // Retrofit 서비스 호출
        val call = retrofitService.uploadMemo(dataPart, filePart)
        Log.d("Retrofit", "Uploading memo data: $dataPart")
        filePart?.let {
            Log.d("Retrofit", "Uploading image: ${filePart.body.contentType()}")
        }

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("Retrofit", "Upload successful, response: $result")
                    //서버 저장 성공시에 기록한 좌표 저장
                    G.userlocation = LatLng(x!!.toDouble(), y!!.toDouble())

                    Toast.makeText(requireContext(), "$result", Toast.LENGTH_SHORT).show()

                    dismiss()
                    (context as MainActivity).binding.bnv.selectedItemId = R.id.bnv_menu_location
                } else {
                    Log.e("Retrofit", "Upload failed, response code: ${response.code()}, error: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("Retrofit", "Network error: ${t.message}", t)
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateMemoToServer(retrofitService: RetrofitService, idMemo: String, x: String?, y: String?, shareMemo: Boolean) {
        val address = binding.addressTv.text.toString()
        val rating = binding.ratingBar.rating.toString()
        val memoText = binding.memoEditText.text.toString()

        // SharedPreferences에서 emailIndex 값을 가져옴
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val emailIndex = sharedPreferences.getInt("email_index", -1).toString()

        // 전송할 String 데이터들을 Map 컬렉션에 저장
        val dataPart: MutableMap<String, String> = mutableMapOf(
            "id_memo" to idMemo,
            "addr_memo" to address,
            "score_memo" to rating,  // rating의 기본값을 0으로 설정
            "text_memo" to memoText,
            "x_memo" to (x ?: ""),
            "y_memo" to (y ?: ""),
            "date_memo" to getCurrentDateTime(),
            "share_memo" to if (shareMemo) "1" else "0",
            "email_index" to emailIndex
        )
        // 이미지 파일을 선택한 경우에만 파일 포장
        val filePart: MultipartBody.Part? = imgPath?.let {
            val file = File(it)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("img_memo", file.name, requestBody)
        }

        // Retrofit 서비스 호출
        val call = retrofitService.updateMemo(dataPart, filePart)
        Log.d("Retrofit", "Updating memo data: $dataPart")
        filePart?.let {
            Log.d("Retrofit", "Uploading image: ${filePart.body.contentType()}")
        }

        // 서버 응답 처리
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "메모가 업로드되었습니다!", Toast.LENGTH_SHORT).show()
                    //서버 저장 성공시에 기록한 좌표 저장
                    G.userlocation = LatLng(x!!.toDouble(), y!!.toDouble())
                    dismiss()
                    (context as MainActivity).binding.bnv.selectedItemId = R.id.bnv_menu_location
                } else {
                    Toast.makeText(requireContext(), "업로드 실패: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




}
