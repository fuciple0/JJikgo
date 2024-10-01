package com.fuciple0.jjikgo.fragments

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.activities.LoginActivity
import com.fuciple0.jjikgo.data.UserResponse
import com.fuciple0.jjikgo.databinding.FragmentMypageBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageFragment : Fragment() {

    private lateinit var binding: FragmentMypageBinding
    private var currentEmailIndex: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 뷰 바인딩 설정
        binding = FragmentMypageBinding.inflate(inflater, container, false)

        // SharedPreferences에서 email_index 값을 가져옴
        currentEmailIndex = getCurrentEmailIndex()

        // 로그인된 사용자의 정보를 서버에서 불러옴
        currentEmailIndex?.let { emailIndex ->
            loadUserInfoFromServer(emailIndex)
            Log.e("mytag", "${emailIndex}")
        }


        // 로그아웃 버튼 클릭 리스너 설정
        binding.btnLogout.setOnClickListener {
            logout()
        }

        return binding.root
    }

    private fun getCurrentEmailIndex(): Int? {
        // SharedPreferences에서 현재 로그인한 사용자의 email_index 값을 가져옴
        val sharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("email_index", -1).takeIf { it != -1 }
    }

    private fun loadUserInfoFromServer(emailIndex: Int) {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        // 서버에서 사용자 정보를 요청
        val call = retrofitService.getUserInfo(emailIndex)  // 서버에서 email_index 기반 사용자 정보 요청
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()

                    user?.let {
                        // 닉네임 설정
                        binding.userNick.text = it.nickname

                        // 프로필 이미지 설정 (절대 경로 사용)
                        val profileImagePath: String? = it.profileImage.toString()
                        if (!profileImagePath.isNullOrEmpty()) {
                            val imgUrl = "http://fuciple0.dothome.co.kr/Jjikgo/$profileImagePath"
                            Glide.with(requireContext()).load(imgUrl).into(binding.userProfile)
                        } else {
                            // 기본 이미지 설정
                            binding.userProfile.setImageResource(R.drawable.user_profile)
                        }


                        // 레벨 설정
                        binding.userLevel.text = "레벨 : ${it.level}"
                    }
                } else {
                    Log.e("MypageFragment", "Response Error: ${response.errorBody()?.string()}")
                    binding.userNick.text = "불러오기 실패"
                    binding.userProfile.setImageResource(R.drawable.user_profile)
                    binding.userLevel.text = "레벨 : 불러오기 실패"
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("MypageFragment", "Request Failed: ${t.message}")
                binding.userNick.text = "불러오기_onFailure"
                binding.userProfile.setImageResource(R.drawable.user_profile)
                binding.userLevel.text = "레벨 : 불러오기_onFailure"
            }
        })
    }


    private fun logout() {
        // SharedPreferences에서 로그인 정보 삭제
        val sharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // 로그인 상태와 관련된 정보를 모두 삭제
        editor.clear()
        editor.apply()

        // LoginActivity로 이동
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish() // 현재 액티비티 종료
    }

}