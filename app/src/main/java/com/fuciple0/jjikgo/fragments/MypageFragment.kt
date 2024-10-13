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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.activities.LoginActivity
import com.fuciple0.jjikgo.adapter.BookmarkedMemoAdapter
import com.fuciple0.jjikgo.adapter.SharedMemoAdapter
import com.fuciple0.jjikgo.data.SharedMemoData
import com.fuciple0.jjikgo.data.ToggleViewModel
import com.fuciple0.jjikgo.data.UserInfoResponse
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

    // 프로필 수정 바텀시트에 보낼 정보
    var profileImageUri:String? = null
    var nickname:String? = null

    var remainingScore:Int = 0

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

        binding.tvEditProfile.setOnClickListener { showEditUserProfileBottomSheet() }

        // 로그인된 사용자의 정보를 서버에서 불러옴
        //loadUserInfoFromServer(G.emailIndex!!.toInt())
       // Log.d("mylog", "${G.emailIndex!!.toInt()}")

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
        //test(G.emailIndex!!.toInt())
        fetchUserInfo(G.emailIndex!!.toInt())

        return binding.root
    }

    private fun showEditUserProfileBottomSheet() {
        // 현재 프로필 이미지와 닉네임을 전달
        val bottomSheet = EditUserProfileBottomSheet.newInstance(profileImageUri, nickname)
        bottomSheet.show(parentFragmentManager, "EditUserProfileBottomSheet")
    }

    private fun fetchUserInfo(emailIndex: Int) {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        // 서버에서 사용자 닉네임, 레벨, 프로필 이미지 정보를 요청
        val call = retrofitService.getUserInfo(emailIndex)
        call.enqueue(object : Callback<UserInfoResponse> {
            override fun onResponse(call: Call<UserInfoResponse>, response: Response<UserInfoResponse>) {
                if (response.isSuccessful) {
                    val userInfoResponse = response.body()

                    userInfoResponse?.let {
                        if (it.status == "success") {
                            // 로그로 모든 정보 출력
                            Log.d("UserInfo", "User nickname: ${it.nickname}")
                            Log.d("UserInfo", "User level: ${it.level}")
                            Log.d("UserInfo", "User profile image: ${it.profile_image}")
                            Log.d("UserInfo", "Total score: ${it.total_score}")
                            Log.d("UserInfo", "Today's score: ${it.today_total_score}")

                            // 닉네임 설정
                            binding.userNick.text = it.nickname
                            nickname = it.nickname

                            // 레벨 설정
                            binding.userLevel.text = "레벨: ${it.level}"

                            // 프로필 이미지 설정 (절대 경로 사용)
                            val profileImagePath = it.profile_image
                            if (!profileImagePath.isNullOrEmpty()) {
                                val imgUrl = "http://fuciple0.dothome.co.kr/Jjikgo/$profileImagePath"
                                Glide.with(requireContext()).load(imgUrl).into(binding.userProfile)
                            } else {
                                // 기본 이미지 설정
                                binding.userProfile.setImageResource(R.drawable.user_profile)
                            }
                            profileImageUri = it.profile_image

                            // 오늘의 점수 설정 (id = tv_point_today)
                            binding.tvPointToday.text = "오늘 적립 ${it.today_total_score ?: 0}점"

                            // 누적 점수 설정 (id = tv_point_score)
                            binding.tvPointScore.text = "누적 ${it.total_score ?: 0}점"

                            // 점수 상세 내역 설정 (id = tv_point_detail)
                            val scoreDetailText = StringBuilder()

                            // score_details가 null 또는 비어있지 않은지 확인
                            if (!it.score_details.isNullOrEmpty()) {
                                it.score_details.forEach { detail ->
                                    scoreDetailText.append("${detail.date_score}          ${detail.tag_score}          ${detail.num_score}점\n")
                                }
                            } else {
                                scoreDetailText.append("점수 내역 없음")
                            }
                            binding.tvPointDetail.text = scoreDetailText.toString().trimEnd()  // 마지막 줄바꿈 제거

                            // 레벨 계산 후 업데이트 로직 추가
                            val totalScore = it.total_score ?: 0
                            val currentLevel = it.level!!.toInt()

                            // 레벨 업데이트
                            updateUserLevel(totalScore, currentLevel, emailIndex)


                            // 닉네임과 현재 레벨을 TextView에 설정
                            binding.tvLevelNotice.text = "${it.nickname} 님 현재 레벨 ${it.level}입니다."

                            // 현재 레벨과 다음 레벨 설정
                            binding.tvNowLevel.text = "레벨: $currentLevel"
                            binding.tvNextLevel.text = "레벨: ${currentLevel + 1}"

                            // 다음 레벨까지의 점수 계산
                            val pointsForNextLevel = pointsForLevel(currentLevel + 1)
                            val pointsForCurrentLevel = pointsForLevel(currentLevel)

                            // progressToNextLevel 값을 Double로 변환
                            val progressToNextLevel = ((remainingScore).toDouble() / (pointsForNextLevel).toDouble()) * 100

                            // 프로그레스바에 진행률 설정
                            binding.roundedProgressBar.setProgressPercentage(progressToNextLevel, true)



                        } else {
                            Log.e("UserInfo", "Error: ${it.status}")
                        }
                    }
                 } else {
                    Log.e("UserInfo", "Response error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
                Log.e("UserInfo", "Request failed: ${t.message}")
            }
        })
    }

    private fun pointsForLevel(level: Int): Int {
        return 10 + (level * 2)
    }

    private fun calculateLevelFromScore(totalScore: Int): Int {
        var level = 1
        var pointsNeeded = pointsForLevel(level)
         remainingScore = totalScore

        while (remainingScore >= pointsNeeded) {
            remainingScore -= pointsNeeded
            level++
            pointsNeeded = pointsForLevel(level)
        }

        return level
    }

    private fun updateUserLevel(totalScore: Int, currentLevel: Int, emailIndex: Int) {
        val newLevel = calculateLevelFromScore(totalScore)

        if (newLevel > currentLevel) {
            val retrofitService = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/").create(RetrofitService::class.java)
            val call = retrofitService.updateUserLevel(emailIndex, newLevel)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("UpdateUserLevel", "User level updated to $newLevel")
                        Toast.makeText(requireContext(), "레벨이 업데이트되었습니다: 레벨 $newLevel", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("UpdateUserLevel", "Failed to update user level.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("UpdateUserLevel", "Error updating user level", t)
                }
            })
        }
    }


    private fun setupRecyclerView() {
        // 북마크된 항목 가져오기
        val bookmarkedMemos = toggleViewModel.getBookmarkedMemos()

        if (bookmarkedMemos.isEmpty()) {
            // 레트로핏 작업으로 서버에서 북마크된 게시물 정보 가져오기
            val retrofitService = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/").create(RetrofitService::class.java)
            retrofitService.getBookmarkedMemos(G.emailIndex!!.toInt()).enqueue(object : Callback<List<SharedMemoData>> {
                override fun onResponse(call: Call<List<SharedMemoData>>, response: Response<List<SharedMemoData>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val newBookmarkedMemos = response.body()!!
                        // ViewModel에 새로운 북마크 항목 추가
                        toggleViewModel.addBookmarkedMemos(newBookmarkedMemos)

                        // RecyclerView에 어댑터 설정
                        val bookMarkedMemoAdapter = BookmarkedMemoAdapter(newBookmarkedMemos.toMutableList(), toggleViewModel)
                        binding.recyclerView.apply {
                            layoutManager = GridLayoutManager(requireContext(), 2) // 그리드 레이아웃으로 2열 설정
                            adapter = bookMarkedMemoAdapter
                        }


                        // 로그로 확인 (디버깅용)
                        Log.d("MypageFragment", "서버에서 가져온 북마크된 메모 수: ${newBookmarkedMemos.size}")
                    }
                }

                override fun onFailure(call: Call<List<SharedMemoData>>, t: Throwable) {
                    Log.e("MypageFragment", "서버에서 데이터를 가져오는데 실패했습니다: ${t.message}")
                }
            })
        } else {
            // 기존 ViewModel의 북마크된 메모 사용
            val bookMarkedMemoAdapter = BookmarkedMemoAdapter(bookmarkedMemos.toMutableList(), toggleViewModel)
            binding.recyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 2) // 그리드 레이아웃으로 2열 설정
                adapter = bookMarkedMemoAdapter
            }

            // 로그로 확인 (디버깅용)
            Log.d("MypageFragment", "캐시된 북마크된 메모 수: ${bookmarkedMemos.size}")
        }
    }



//    private fun loadUserInfoFromServer(emailIndex: Int) {
//        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
//        val retrofitService = retrofit.create(RetrofitService::class.java)
//
//        // 서버에서 사용자 정보를 요청
//        val call = retrofitService.getUserInfo(emailIndex)  // 서버에서 email_index 기반 사용자 정보 요청
//        call.enqueue(object : Callback<UserResponse> {
//            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
//                if (response.isSuccessful) {
//                    val user = response.body()
//
//                    user?.let {
//                        // 로그로 닉네임, 레벨, 프로필 이미지 경로, 적립완료 점수, 누적 점수를 출력
//                        Log.d("MypageFragment1", "Nickname: ${it.nickname_user}")
//                        Log.d("MypageFragment1", "Level: ${it.level_user}")
//                        Log.d("MypageFragment1", "Profile Image Path: ${it.profileimg_user}")
//                        Log.d("MypageFragment1", "Today's Points: ${it.today_total_score}")
//                        Log.d("MypageFragment1", "Total Points: ${it.total_score}")
//
//                        // 닉네임 설정
//                        binding.userNick.text = it.nickname_user
//
//                        // 프로필 이미지 설정 (절대 경로 사용)
////                        val profileImagePath = it.profileimg_user  // profileimg_user가 null일 경우 null 그대로 유지
////                        if (!profileImagePath.isNullOrEmpty()) {
////                            val imgUrl = "http://fuciple0.dothome.co.kr/Jjikgo/$profileImagePath"
////                            Glide.with(requireContext()).load(imgUrl).into(binding.userProfile)
////                        } else {
////                            // 기본 이미지 설정
////                            binding.userProfile.setImageResource(R.drawable.user_profile)
////                        }
//
////                        // 레벨 설정
////                        binding.userLevel.text = "레벨 : ${it.level_user}"
////
////                        // 오늘 적립 점수 설정 (id = tv_point_today)
////                        binding.tvPointToday.text = "적립완료 ${it.today_total_score}점"
////
////                        // 누적 점수 설정 (id = tv_point_score)
////                        binding.tvPointScore.text = "누적 ${it.total_score}점"
//
//                        // 점수 상세 내역 설정 (id = tv_point_detail)
//                        val scoreDetailText = StringBuilder()
//
//                        // score_details가 null 또는 비어있지 않은지 확인
////                        if (!it.score_details.isNullOrEmpty()) {
////                            it.score_details.forEach { detail ->
////                                scoreDetailText.append("${detail.date_score}  ${detail.tag_score} ${detail.num_score}점\n")
////                            }
////                        } else {
////                            scoreDetailText.append("점수 내역 없음")
////                        }
////                        binding.tvPointDetail.text = scoreDetailText.toString().trimEnd()  // 마지막 줄바꿈 제거
//                    }
//                } else {
//                    Log.e("MypageFragment", "Response Error: ${response.errorBody()?.string()}")
//                    binding.userNick.text = "불러오기 실패"
//                    binding.userProfile.setImageResource(R.drawable.user_profile)
//                    binding.userLevel.text = "레벨 : 불러오기 실패"
//                    binding.tvPointToday.text = "적립완료 0점"
//                    binding.tvPointScore.text = "누적 0점"
//                    binding.tvPointDetail.text = "점수 내역 없음"
//                }
//            }
//
//            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
//                Log.e("MypageFragment", "Request Failed: ${t.message}")
//                binding.userNick.text = "불러오기_onFailure"
//                binding.userProfile.setImageResource(R.drawable.user_profile)
//                binding.userLevel.text = "레벨 : 불러오기_onFailure"
//                binding.tvPointToday.text = "적립완료 0점"
//                binding.tvPointScore.text = "누적 0점"
//                binding.tvPointDetail.text = "점수 내역 없음"
//            }
//        })
//    }



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


    // Fragment가 일시 중지 상태로 들어갈 때 호출
    override fun onPause() {
        super.onPause()
        // 화면이 벗어나면 북마크 상태를 서버로 전송
        toggleViewModel.submitAllChanges()
    }

}