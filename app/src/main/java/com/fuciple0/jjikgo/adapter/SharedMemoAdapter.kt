package com.fuciple0.jjikgo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        holder.binding.tvLike.text = "좋아요 ${sharedMemo.likeCount}"

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
    }

    override fun getItemCount(): Int {
        return sharedMemoList.size
    }
}


