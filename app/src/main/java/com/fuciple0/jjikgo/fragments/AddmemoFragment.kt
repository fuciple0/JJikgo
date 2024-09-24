package com.fuciple0.jjikgo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fuciple0.jjikgo.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AddmemoFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_addmomo_bottomsheet, container, false)

        // 전달받은 주소 값
        val addressMemo = arguments?.getString("addressMemo")

        // TextView에 주소 표시
        view.findViewById<TextView>(R.id.address_tv).text = addressMemo

        return view
    }
}
