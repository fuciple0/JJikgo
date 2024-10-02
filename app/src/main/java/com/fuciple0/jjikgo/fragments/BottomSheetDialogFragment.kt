package com.fuciple0.jjikgo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.MemoResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MemoDetailsBottomSheetFragment : BottomSheetDialogFragment() {

    private var memo: MemoResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_memo_details_bottomsheet, container, false)

        // 메모 데이터를 받아와서 UI 업데이트
//        memo?.let {
//            view.findViewById<TextView>(R.id.memo_address).text = "주소: ${it.addr_memo}"
//            view.findViewById<TextView>(R.id.memo_score).text = "점수: ${it.score_memo}"
//            view.findViewById<TextView>(R.id.memo_content).text = "내용: ${it.text_memo}"
//            view.findViewById<TextView>(R.id.memo_date).text = "날짜: ${it.date_memo}"
//            // 이미지가 있는 경우 Glide로 로드
//            val imageView = view.findViewById<ImageView>(R.id.memo_image)
//            if (!it.img_memo.isNullOrEmpty()) {
//                val fullImageUrl = "http://fuciple0.dothome.co.kr/Jjikgo/${it.img_memo}"
//                Glide.with(this)
//                    .load(fullImageUrl)
//                    .placeholder(R.drawable.no_image)  // 기본 이미지 설정
//                    .into(imageView)
//            } else {
//                imageView.setImageResource(R.drawable.no_image)  // 이미지가 없을 경우 기본 이미지 설정
//            }
//        }
        return view
    }

    fun setMemoData(memo: MemoResponse) {
        this.memo = memo
    }
}
