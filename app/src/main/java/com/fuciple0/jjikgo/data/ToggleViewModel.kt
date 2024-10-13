package com.fuciple0.jjikgo.data

import android.util.Log
import androidx.lifecycle.ViewModel
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToggleViewModel : ViewModel() {
    private val baseUrl = "http://fuciple0.dothome.co.kr/"
    private val retrofitService: RetrofitService = RetrofitHelper.getRetrofitInstance(baseUrl).create(RetrofitService::class.java)

    // G 클래스에서 전역 변수로 정의된 emailIndex (String) -> int로 변환
    private val emailIndex: Int = G.emailIndex!!.toIntOrNull() ?: -1  // 변환 실패 시 -1 설정

    // 북마크, 팔로우, 좋아요 상태가 변경된 메모들
    private val pendingBookmarkMemos = mutableListOf<SharedMemoData>()
    private val pendingFollowMemos = mutableListOf<SharedMemoData>()
    private val pendingLikeMemos = mutableListOf<SharedMemoData>()

    // 이미 북마크된 메모들 (UI에서 사용, 초기화되지 않음)
    private val bookmarkedMemos = mutableListOf<SharedMemoData>()

    // 추가된 부분: addBookmarkedMemos 메서드
    // 서버에서 가져온 북마크된 메모를 추가하는 메서드
    fun addBookmarkedMemos(newMemos: List<SharedMemoData>) {
        // 현재 북마크된 메모 목록을 가져와 새로운 메모 추가
        val updatedMemos = bookmarkedMemos.toMutableList()
        updatedMemos.addAll(newMemos)
        bookmarkedMemos.clear()
        bookmarkedMemos.addAll(updatedMemos)
    }


    // 구조적으로 수정해야함 ***

    // 북마크 상태가 변경될 때 호출 (서버 전송 X)
    fun updateBookmarkState(isBookmarked: Boolean, memo: SharedMemoData) {
        if (isBookmarked) {
            // 북마크 추가
            bookmarkedMemos.add(memo)
            pendingBookmarkMemos.add(memo)  // 전송 대기 리스트에도 추가
        } else {
            // 북마크 해제
            bookmarkedMemos.remove(memo)
            pendingBookmarkMemos.remove(memo)  // 북마크 해제도 전송 대기 리스트에 추가
        }
    }

    // 팔로우 상태가 변경될 때 호출 (서버 전송 X)
    fun updateFollowState(isFollowing: Boolean, memo: SharedMemoData) {
        if (isFollowing) {
            pendingFollowMemos.add(memo)
        } else {
            pendingFollowMemos.remove(memo)
        }
    }

    // 좋아요 상태가 변경될 때 호출 (서버 전송 X)
    fun updateLikeState(isLiked: Boolean, memo: SharedMemoData) {
        if (isLiked) {
            pendingLikeMemos.add(memo)
        } else {
            pendingLikeMemos.remove(memo)
        }
    }


    // 서버로 모든 변경 사항을 한 번에 전송 (화면 이동 시 호출)
    fun submitAllChanges() {
        submitBookmarkChanges()
        submitFollowChanges()
        submitLikeChanges()
    }


    // 북마크 상태 전송
    private fun submitBookmarkChanges() {
        Log.d("ToggleViewModel", "서버로 전송할 북마크 메모 개수: ${pendingBookmarkMemos.size}")
        for (memo in pendingBookmarkMemos) {
            val call = retrofitService.updateBookmarkStatus(memo.id_memo, emailIndex, true)
            call.enqueue(callbackHandler("북마크 상태 전송"))
        }
        pendingBookmarkMemos.clear()  // 서버 전송 후 리스트 초기화
    }

    // 팔로우 상태 전송
    private fun submitFollowChanges() {
        Log.d("ToggleViewModel", "서버로 전송할 팔로우 상태 변경: ${pendingFollowMemos.size}")
        for (memo in pendingFollowMemos) {
            val call = retrofitService.updateFollowStatus(emailIndex, memo.target_email_index, true)
            call.enqueue(callbackHandler("팔로우 상태 전송"))
        }
        pendingFollowMemos.clear()
    }

    // 좋아요 상태 전송
    private fun submitLikeChanges() {
        Log.d("ToggleViewModel", "서버로 전송할 좋아요 상태 변경: ${pendingLikeMemos.size}")
        for (memo in pendingLikeMemos) {
            val call = retrofitService.updateLikeStatus(memo.id_memo, emailIndex, true)
            call.enqueue(callbackHandler("좋아요 상태 전송"))
        }
        pendingLikeMemos.clear()
    }

    // 공통 응답 처리 콜백
    private fun callbackHandler(action: String): Callback<Void> {
        return object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("ToggleViewModel", "$action 성공")
                } else {
                    Log.e("ToggleViewModel", "$action 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ToggleViewModel", "$action 실패: ${t.message}")
            }
        }
    }

    // 북마크된 메모들 가져오기 (다른 페이지에서 사용 가능)
    fun getBookmarkedMemos(): List<SharedMemoData> {
        return bookmarkedMemos
    }



}
