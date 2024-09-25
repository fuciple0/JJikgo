package com.fuciple0.jjikgo.fragments

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.MemoDatabaseHelper
import com.fuciple0.jjikgo.databinding.FragmentAddmemoBottomsheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class AddmemoFragment : BottomSheetDialogFragment() {

    private lateinit var dbHelper: MemoDatabaseHelper
    // ViewBinding 객체 선언
    private var _binding: FragmentAddmemoBottomsheetBinding? = null
    private val binding get() = _binding!!
    // 멤버변수 - 업로드할 이미지의 절대 주소를 저장하는 문자열 참조변수
    var imgPath:String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddmemoBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = MemoDatabaseHelper(requireContext())

        // 전달받은 주소 값 표시
        val addressMemo = arguments?.getString("addressMemo")
        val x = arguments?.getString("x")
        val y = arguments?.getString("y")
        binding.addressTv.text = addressMemo

        // saveMemoButton1에 리스너 추가
        binding.saveMemoButton1.setOnClickListener {
            saveMemoToDatabase(x, y) // x, y 좌표 함께 저장
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

    private fun saveMemoToDatabase(x: String?, y: String?) {
        val address = binding.addressTv.text.toString()
        val rating = binding.ratingBar.rating
        val memoText = binding.memoEditText.text.toString()

        // 이미지 경로
        val imagePath = imgPath

        if (address.isNotEmpty() && memoText.isNotEmpty() && imagePath != null) {
            val id = dbHelper.insertMemo(address, rating, imagePath, memoText, x, y) // x, y 좌표 추가
            if (id != -1L) {
                Toast.makeText(requireContext(), "메모가 저장되었습니다!", Toast.LENGTH_SHORT).show()
                dismiss() // 저장 후 바텀시트 닫기
            } else {
                Toast.makeText(requireContext(), "저장 실패!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
