package com.fuciple0.jjikgo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.MemoDatabaseHelper.Memo

class MemoAdapter(private val memoList: List<Memo>) : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    class MemoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv)
        val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        val timeTextView: TextView = itemView.findViewById(R.id.tv_time)
        val ratingTextView: TextView = itemView.findViewById(R.id.tv_rating)
        val bodyTextView: TextView = itemView.findViewById(R.id.tv_body)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_myitem, parent, false)
        return MemoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = memoList[position]
        holder.titleTextView.text = memo.address // 주소를 제목으로 표시
        holder.timeTextView.text = memo.dateTime // 저장된 날짜 및 시간
        holder.ratingTextView.text = memo.rating.toInt().toString() // 평점 표시
        holder.bodyTextView.text = memo.memo // 메모 내용 표시

        // 이미지 로드
        Glide.with(holder.imageView.context)
            .load(memo.imagePath)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return memoList.size
    }
}
