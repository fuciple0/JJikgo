package com.fuciple0.jjikgo.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.adapter.SharedMemoAdapter
import com.fuciple0.jjikgo.data.MemoResponse
import com.fuciple0.jjikgo.data.SharedMemoData
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YourlistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedMemoAdapter: SharedMemoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_yourlist, container, false)

        // RecyclerView 설정
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 서버에서 공유 메모 불러오기
        loadSharedMemos()

        return view
    }

    // 서버에서 공유된 메모 불러오는 메서드
    private fun loadSharedMemos() {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val call = retrofitService.getSharedMemoData()
        call.enqueue(object : Callback<List<SharedMemoData>> {
            override fun onResponse(call: Call<List<SharedMemoData>>, response: Response<List<SharedMemoData>>) {
                if (response.isSuccessful) {
                    val sharedMemoList = response.body()
                    // 리스트를 역순으로 정렬하여 최신 메모가 맨 위에 오도록 설정
                    sharedMemoList?.let {
                        val reversedList = it.reversed()  // 리스트를 역순으로 정렬
                        sharedMemoAdapter = SharedMemoAdapter(reversedList)
                        recyclerView.adapter = sharedMemoAdapter
                    }
                } else {
                    Log.e("YourlistFragment", "Failed to load shared memos: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<SharedMemoData>>, t: Throwable) {
                Log.e("YourlistFragment", "Network error: ${t.message}")
            }
        })
    }

}
