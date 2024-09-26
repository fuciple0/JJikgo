package com.fuciple0.jjikgo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.adapters.MemoAdapter
import com.fuciple0.jjikgo.data.MemoDatabaseHelper

class MylistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var memoAdapter: MemoAdapter
    private lateinit var dbHelper: MemoDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mylist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dbHelper = MemoDatabaseHelper(requireContext())

        loadMemos()
    }

    private fun loadMemos() {
        val memos = dbHelper.getAllMemos() // 데이터베이스에서 모든 메모 가져오기
        memoAdapter = MemoAdapter(memos) // 어댑터 생성
        recyclerView.adapter = memoAdapter // 리사이클러뷰에 어댑터 설정
    }
}
