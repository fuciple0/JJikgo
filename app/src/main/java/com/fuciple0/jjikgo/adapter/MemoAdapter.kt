package com.fuciple0.jjikgo.adapter

import com.bumptech.glide.Glide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fuciple0.jjikgo.data.MemoResponse
import com.fuciple0.jjikgo.databinding.RecyclerMyitem2Binding
import com.fuciple0.jjikgo.databinding.RecyclerMyitemBinding


class MemoAdapter(
    private var memoList: MutableList<MemoResponse>,
    private val onMemoClick: (MemoResponse) -> Unit  // 클릭 리스너 콜백 추가
) : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    // 기존 데이터 업데이트 메서드
    fun updateMemoList(newMemos: List<MemoResponse>) {
        memoList = newMemos.toMutableList()
        notifyDataSetChanged()  // RecyclerView를 새로고침
    }

    // 새로운 데이터를 기존 리스트에 추가하는 메서드
    fun addMemoList(newMemos: List<MemoResponse>) {
        val startPosition = memoList.size
        memoList.addAll(newMemos)
        notifyItemRangeInserted(startPosition, newMemos.size)  // 새로 추가된 항목만 새로고침
    }

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

        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onMemoClick(memo)  // 클릭된 메모 데이터 전달
        }

    }

    override fun getItemCount(): Int = memoList.size

}




