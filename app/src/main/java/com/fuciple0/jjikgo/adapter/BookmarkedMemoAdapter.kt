package com.fuciple0.jjikgo.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.SharedMemoData
import com.fuciple0.jjikgo.data.ToggleViewModel
import com.fuciple0.jjikgo.databinding.RecyclerBookmarkItemBinding
import com.fuciple0.jjikgo.databinding.RecyclerShareItemBinding

class BookmarkedMemoAdapter(
    private var sharedMemoList: MutableList<SharedMemoData>,
    private val toggleViewModel: ToggleViewModel  // ViewModel 주입
) : RecyclerView.Adapter<BookmarkedMemoAdapter.MemoViewHolder>() {

    // ViewHolder 클래스 정의
    inner class MemoViewHolder(val binding: RecyclerBookmarkItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = RecyclerBookmarkItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(binding)
    }
    override fun getItemCount(): Int {
        return sharedMemoList.size
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val sharedMemo = sharedMemoList[position]

        // 자신의 글인지 확인하기 위해 로그인된 사용자와 글 작성자 비교
        val isMyMemo = G.emailIndex?.toInt() == sharedMemo.target_email_index




        // 초기 북마크 상태에 따른 아이콘 설정
        if (sharedMemo.isBookmarked == 1) {
            holder.binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled)  // 북마크 활성화
        } else {
            holder.binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border)  // 북마크 비활성화
        }





        // 사용자 정보 바인딩
        holder.binding.tvNickname.text = sharedMemo.nickname_user
        holder.binding.tvLevel.text = "레벨: ${sharedMemo.level_user}"


        // 메모 정보 바인딩
        holder.binding.tvBody.text = sharedMemo.text_memo
        holder.binding.tvTime.text = sharedMemo.date_memo.split(" ")[0]
        holder.binding.tvAddr.text = sharedMemo.addr_memo  // 주소 바인딩 추가

        // 프로필 이미지 로드 (Glide 사용)
        Glide.with(holder.itemView.context)
            .load("http://fuciple0.dothome.co.kr/Jjikgo/${sharedMemo.profileimg_user}")
            .into(holder.binding.userProfile)

        // 메모 이미지 로드 (Glide 사용), 이미지가 없을 경우 ImageView 숨김
        if (sharedMemo.img_memo.isNullOrEmpty()) {
            holder.binding.iv.visibility = View.GONE // 이미지가 없으면 ImageView 숨김
            holder.binding.bookmarkIcon.visibility = View.GONE
        } else {
            holder.binding.iv.visibility = View.VISIBLE // 이미지가 있으면 보여줌
            Glide.with(holder.itemView.context)
                .load("http://fuciple0.dothome.co.kr/Jjikgo/${sharedMemo.img_memo}")
                .into(holder.binding.iv)
        }


        // 북마크 클릭 이벤트 처리
        holder.binding.bookmarkIcon.setOnClickListener {
            // 상태를 1(북마크됨) 또는 0(북마크되지 않음)으로 토글
            sharedMemo.isBookmarked = if (sharedMemo.isBookmarked == 1) 0 else 1
            toggleViewModel.updateBookmarkState(sharedMemo.isBookmarked == 1, sharedMemo)  // ViewModel에 저장 (true/false로 변환)

            // UI 업데이트
            if (sharedMemo.isBookmarked == 1) {
                holder.binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled)
                Toast.makeText(holder.itemView.context, "스크랩하였습니다.", Toast.LENGTH_SHORT).show()
            } else {
                holder.binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border)
                Toast.makeText(holder.itemView.context, "스크랩을 취소하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // 기존 리스트에 새로운 메모 리스트를 추가하는 메서드 추가
    fun addMemoList(newMemos: List<SharedMemoData>) {
        val startPosition = sharedMemoList.size
        sharedMemoList.addAll(newMemos)  // 새로운 메모 리스트 추가
        notifyItemRangeInserted(startPosition, newMemos.size)  // 추가된 항목만 새로고침
    }

}
