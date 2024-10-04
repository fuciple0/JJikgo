package com.fuciple0.jjikgo.adapter

import com.bumptech.glide.Glide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fuciple0.jjikgo.data.MemoResponse
import com.fuciple0.jjikgo.databinding.RecyclerMyitem2Binding
import com.fuciple0.jjikgo.databinding.RecyclerMyitemBinding


class MemoAdapter(private var memoList: MutableList<MemoResponse>) : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    class MemoViewHolder(val binding: RecyclerMyitem2Binding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = RecyclerMyitem2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = memoList[position]
        holder.binding.tvAddr.text = memo.addr_memo
        holder.binding.tvTime.text = memo.date_memo
        holder.binding.tvRating.text = memo.score_memo.toString()
        holder.binding.tvBody.text = memo.text_memo

        // 프로필 이미지 로드 (Glide 사용)
//        Glide.with(holder.itemView.context)
//            .load("http://fuciple0.dothome.co.kr/Jjikgo/${memo.email_index}")
//            .into(holder.binding.userProfile)

        // 메모 이미지 로드 (Glide 사용), 이미지가 없을 경우 ImageView 숨김
        if (memo.img_memo.isNullOrEmpty()) {
            holder.binding.iv.visibility = View.GONE
        } else {
            holder.binding.iv.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load("http://fuciple0.dothome.co.kr/Jjikgo/${memo.img_memo}")
                .into(holder.binding.iv)
        }
    }

    override fun getItemCount(): Int = memoList.size

    // ViewModel의 LiveData와 연결되는 메서드 수정
    fun updateMemoList(newMemoList: List<MemoResponse>) {
        memoList.clear()
        memoList.addAll(newMemoList)
        notifyDataSetChanged()
    }
}




