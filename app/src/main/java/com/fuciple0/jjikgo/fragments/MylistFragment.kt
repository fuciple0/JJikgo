package com.fuciple0.jjikgo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.activities.MainActivity
import com.fuciple0.jjikgo.adapter.DummyAdapter
import com.fuciple0.jjikgo.data.DummyItem
import com.fuciple0.jjikgo.databinding.FragmentMylistBinding

class MylistFragment : Fragment() {

    lateinit var binding :FragmentMylistBinding

    var dummyList : MutableList<DummyItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //여기서 작업시작

        // 더미 데이터 추가
        dummyList.add(DummyItem("김치찌개 청담점", "김치찌개 국물이 아주 끝내줌, 아주 시원하고 소주가 아주 꿀떢꿀떡 들어감. 너무 맛있어서 똑 가고싶음", 5, R.drawable.dummy_1))
        dummyList.add(DummyItem("참치찌개 성수점", "참치찌개 국물이 아주 끝내줌, 비도 오고, 술한잔이 생각이 난다. 이시간이 되면", 3, R.drawable.dummy_2))
        dummyList.add(DummyItem("부대찌개 왕십리점", "부대찌개 국물이 아주 끝내줌", 4, R.drawable.dummy_3))
        dummyList.add(DummyItem("동태찌개 구리점", "동태찌개 국물이 아주 끝내줌", 2, R.drawable.dummy_4))
        dummyList.add(DummyItem("동태찌개 인창점", "동태찌개 국물이 아주 끝내줌", 1, R.drawable.dummy_4))
        dummyList.add(DummyItem("동태찌개 토평점", "동태찌개 국물이 아주 끝내줌", 2, R.drawable.dummy_4))
        dummyList.add(DummyItem("동태찌개 교문점", "동태찌개 국물이 아주 끝내줌", 3, R.drawable.dummy_4))
        dummyList.add(DummyItem("동태찌개 덕소점", "동태찌개 국물이 아주 끝내줌", 4, R.drawable.dummy_4))
        dummyList.add(DummyItem("동태찌개 마석점", "동태찌개 국물이 아주 끝내줌", 5, R.drawable.dummy_4))

        // LayoutManager 설정
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        // Adapter 설정
        binding.recyclerView.adapter = DummyAdapter(requireContext(), dummyList)

        // 변경사항을 알림
        binding.recyclerView.adapter?.notifyDataSetChanged()


    }


}