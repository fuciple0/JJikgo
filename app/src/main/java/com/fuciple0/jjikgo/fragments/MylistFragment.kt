package com.fuciple0.jjikgo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.adapter.MemoAdapter
import com.fuciple0.jjikgo.data.MemoViewModel
import com.fuciple0.jjikgo.databinding.FragmentMylistBinding

class MylistFragment : Fragment() {

    private var _binding: FragmentMylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var memoAdapter: MemoAdapter
    private val memoViewModel: MemoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        memoAdapter = MemoAdapter(mutableListOf())
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) // 2열의 불규칙한 그리드
            adapter = memoAdapter
        }

        // ViewModel에서 메모 데이터를 관찰하여 업데이트
        memoViewModel.memoList.observe(viewLifecycleOwner, Observer { memos ->
            memoAdapter.updateMemoList(memos ?: mutableListOf())  // 데이터가 null일 경우 빈 리스트로 대체
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

