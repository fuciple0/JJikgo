package com.fuciple0.jjikgo.fragments

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.activities.LoginActivity
import com.fuciple0.jjikgo.adapter.SharedMemoAdapter
import com.fuciple0.jjikgo.data.ToggleViewModel
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

    private lateinit var toggleViewModel: ToggleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 뷰 바인딩 설정
        binding = FragmentMypageBinding.inflate(inflater, container, false)
        // ViewModel 초기화 (북마크된 항목을 가져오기 위해 사용)
        toggleViewModel = ViewModelProvider(requireActivity()).get(ToggleViewModel::class.java)
        // RecyclerView 설정
        setupRecyclerView()


        // SharedPreferences에서 email_index 값을 가져옴
        currentEmailIndex = getCurrentEmailIndex()

        // 로그인된 사용자의 정보를 서버에서 불러옴
        currentEmailIndex?.let { emailIndex ->
            loadUserInfoFromServer(emailIndex)
            Log.e("mytag", "${emailIndex}")
        }

        // 툴바 클릭 리스너
        binding.toolbar.setOnClickListener {
            // 툴바가 클릭되었을 때 처리할 작업
            Log.d("MypageFragment", "Toolbar clicked")
        }

        // 툴바 메뉴 아이템 클릭 리스너
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menu_logout) {
                showLogoutConfirmationDialog()  // 로그아웃 확인 다이얼로그 호출
                true
            } else {
                false
            }
        }

        test(G.emailIndex!!.toInt())

        return binding.root
    }

    private fun setupRecyclerView() {
        // 북마크된 항목 가져오기
        val bookmarkedMemos = toggleViewModel.getBookmarkedMemos()

        // RecyclerView에 어댑터 설정
        val sharedMemoAdapter = SharedMemoAdapter(bookmarkedMemos.toMutableList(), toggleViewModel)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sharedMemoAdapter
        }

        // 로그로 확인 (디버깅용)
        Log.d("MypageFragment", "북마크된 메모 수: ${bookmarkedMemos.size}")
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


    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("로그아웃")
        builder.setMessage("정말 로그아웃 하시겠습니까?")

        // "확인" 버튼
        builder.setPositiveButton("확인") { dialog, _ ->
            logout()  // 확인을 누르면 로그아웃 메서드 호출
            dialog.dismiss()
        }

        // "취소" 버튼
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()  // 취소를 누르면 다이얼로그만 닫힘
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun test(emailIndex: Int) {

        // RetrofitHelper를 사용하여 Retrofit 인스턴스 생성
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        // API 호출
        val call = retrofitService.get998(emailIndex)
        call.enqueue(object : Callback<String> {  // 응답을 String 타입으로 받음
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // 원시 JSON 문자열 그대로 출력
                    val rawJson = response.body()
                    rawJson?.let {
                        // 긴 로그를 나누어서 출력하는 부분
                        val maxLogSize = 1500  // 한 번에 출력할 최대 길이
                        for (i in 0..it.length / maxLogSize) {
                            val start = i * maxLogSize
                            val end = if (start + maxLogSize > it.length) it.length else start + maxLogSize
                            Log.d("Test4885", it.substring(start, end))
                        }
                    }
                    // RecyclerView나 지도에 데이터를 반영하는 메소드 호출
                    // updateRecyclerView(memoList)
                } else {
                    Log.e("TestFragment", "Failed to load memos: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("TestFragment", "Network error: ${t.message}")
            }
        })
    }

}