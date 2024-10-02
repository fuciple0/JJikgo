package com.fuciple0.jjikgo.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.MemoDatabaseHelper.Memo
import com.google.android.material.imageview.ShapeableImageView

class MemoAdapter(private val memoList: List<Memo>) : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    class MemoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.iv)
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

        // BLOB에서 비트맵 생성 후 이미지뷰에 설정
        memo.imageBlob?.let { blob ->
            val bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.size)
            holder.imageView.setImageBitmap(bitmap)
        } ?: run {
            holder.imageView.setImageResource(R.drawable.no_image) // 기본 이미지
        }
    }

    override fun getItemCount(): Int {
        return memoList.size
    }
}
