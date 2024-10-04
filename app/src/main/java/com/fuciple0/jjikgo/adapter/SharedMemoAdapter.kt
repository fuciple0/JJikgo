package com.fuciple0.jjikgo.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.SharedMemoData
import com.fuciple0.jjikgo.databinding.RecyclerShareItemBinding

class SharedMemoAdapter(private val sharedMemoList: List<SharedMemoData>) :
    RecyclerView.Adapter<SharedMemoAdapter.MemoViewHolder>() {

    inner class MemoViewHolder(val binding: RecyclerShareItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = RecyclerShareItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val sharedMemo = sharedMemoList[position]

        // 로그를 추가하여 데이터를 확인
        Log.d("SharedMemoAdapter99", "Position: $position, Nickname: ${sharedMemo.nickname}, Level: ${sharedMemo.level}, MemoCount: ${sharedMemo.memoCount}, ScoreAverage: ${sharedMemo.scoreAverage}, FollowerCount: ${sharedMemo.followerCount}, LikeCount: ${sharedMemo.likeCount}")


        // 사용자 정보 바인딩
        holder.binding.tvNickname.text = sharedMemo.nickname
        holder.binding.tvLevel.text = "레벨: ${sharedMemo.level}"
        holder.binding.tvMemoNum.text = "후기 ${sharedMemo.memoCount}"
        holder.binding.tvScore.text = "별점평균 ${sharedMemo.scoreAverage}"
        holder.binding.tvFollower.text = "팔로워 ${sharedMemo.followerCount}"
        holder.binding.tvLike.text = "${sharedMemo.likeCount}"

        // 메모 정보 바인딩
        holder.binding.tvBody.text = sharedMemo.memoText
        holder.binding.tvTime.text = sharedMemo.memoDate
        holder.binding.tvAddr.text = sharedMemo.addrMemo  // 주소 바인딩 추가

        // 프로필 이미지 로드 (Glide 사용)
        Glide.with(holder.itemView.context)
            .load("http://fuciple0.dothome.co.kr/Jjikgo/${sharedMemo.userProfile}")
            .into(holder.binding.userProfile)

        // 메모 이미지 로드 (Glide 사용)
        Glide.with(holder.itemView.context)
            .load("http://fuciple0.dothome.co.kr/Jjikgo/${sharedMemo.memoImage}")
            .into(holder.binding.iv)

        // 북마크 상태 초기값 설정 (필요시 저장된 상태를 가져올 수 있음)
        var isBookmarked = false

        // 북마크 아이콘 클릭 이벤트 처리
        holder.binding.bookmarkIcon.setOnClickListener {
            isBookmarked = !isBookmarked
            if (isBookmarked) {
                // 북마크 설정 (채워진 아이콘으로 변경)
                holder.binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled)
                Toast.makeText(holder.itemView.context, "스크랩하였습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 북마크 해제 (빈 아이콘으로 변경)
                holder.binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border)
                Toast.makeText(holder.itemView.context, "스크랩을 취소하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 팔로우 버튼 로직
        var isFollowing = false // 팔로우 상태

        holder.binding.tvFollow.setOnClickListener {
            val nickname = sharedMemo.nickname
            if (isFollowing) {
                // 팔로잉 상태에서 팔로우 해제
                holder.binding.tvFollow.text = "팔로우"
                holder.binding.tvFollow.setTextColor(Color.parseColor("#2B5CBF")) // 파란색 글씨
                holder.binding.tvFollow.background = holder.itemView.resources.getDrawable(R.drawable.bg_box_bluelight, null) // 파란색 배경
                Toast.makeText(holder.itemView.context, "$nickname 님을 팔로우 취소합니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 팔로우 상태에서 팔로잉으로 변경
                holder.binding.tvFollow.text = "팔로잉"
                holder.binding.tvFollow.setTextColor(holder.itemView.resources.getColor(R.color.black, null)) // 검정색 글씨
                holder.binding.tvFollow.background = holder.itemView.resources.getDrawable(R.drawable.bg_box_gray, null) // 회색 배경
                Toast.makeText(holder.itemView.context, "$nickname 님을 팔로우 합니다.", Toast.LENGTH_SHORT).show()
            }
            isFollowing = !isFollowing // 상태 토글
        }



    }

    override fun getItemCount(): Int {
        return sharedMemoList.size
    }
}


