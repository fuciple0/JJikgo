package com.fuciple0.jjikgo.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.activities.LoginActivity
import com.fuciple0.jjikgo.data.MemoDatabaseHelper
import com.fuciple0.jjikgo.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    private lateinit var binding: FragmentMypageBinding
    private lateinit var dbHelper: MemoDatabaseHelper
    private var currentUserId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 뷰 바인딩 설정
        binding = FragmentMypageBinding.inflate(inflater, container, false)
        dbHelper = MemoDatabaseHelper(requireContext())

        // 현재 로그인한 사용자 ID를 가져옴
        currentUserId = getCurrentUserId()

        // 로그인된 사용자가 있다면 사용자 정보를 불러옴
        currentUserId?.let { loadUserInfo(it) }

        // 로그아웃 버튼 클릭 리스너 설정
        binding.btnLogout.setOnClickListener {
            logout()
        }

        return binding.root
    }

    private fun getCurrentUserId(): Int? {
        // 세션에서 현재 로그인한 사용자 ID를 가져옵니다.
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT ${MemoDatabaseHelper.COLUMN_SESSION_USER_ID} FROM ${MemoDatabaseHelper.TABLE_SESSION}", null)

        var userId: Int? = null
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(MemoDatabaseHelper.COLUMN_SESSION_USER_ID))
        }
        cursor.close()
        db.close()

        return userId
    }

    private fun loadUserInfo(userId: Int) {
        val user = dbHelper.getUserInfo(userId)

        user?.let {
            // 닉네임 설정
            binding.userNick.text = it.nickname

            // 프로필 이미지 설정
            if (it.profileImage != null) {
                val bitmap = BitmapFactory.decodeByteArray(it.profileImage, 0, it.profileImage.size)
                binding.userProfile.setImageBitmap(bitmap)
            } else {
                // 프로필 이미지가 없을 경우 기본 이미지 설정
                binding.userProfile.setImageResource(R.drawable.user_profile)
            }
        }
    }

    private fun logout() {
        // SQLite에서 현재 로그인한 사용자 세션 정보를 삭제
        dbHelper.logoutUser()

        // LoginActivity로 이동
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish() // 현재 액티비티 종료
    }
}