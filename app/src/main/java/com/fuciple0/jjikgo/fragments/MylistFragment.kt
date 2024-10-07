package com.fuciple0.jjikgo.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.adapter.MemoAdapter
import com.fuciple0.jjikgo.data.MemoResponse
import com.fuciple0.jjikgo.data.MemoViewModel
import com.fuciple0.jjikgo.databinding.FragmentMylistBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MylistFragment : Fragment() {

    private var _binding: FragmentMylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var memoAdapter: MemoAdapter
    private val memoViewModel: MemoViewModel by activityViewModels()

    private var isLoading = false  // 데이터가 로드 중인지 확인하는 변수
    private var currentPage = 1    // 현재 페이지
    private val pageSize = 16      // 한 번에 불러올 데이터 개수

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()    // 스피너 설정
        setupRecyclerView() // RecyclerView 설정
        observeNearbyMemos()  // ViewModel 데이터 관찰
        setupRecyclerViewScrollListener()  // 스크롤 리스너 설정
    }

    private fun setupSpinner() {
        val spinner: Spinner = binding.materialToolbar.findViewById(R.id.toolbar_spinner)
        val sortOptions = resources.getStringArray(R.array.menu_items)
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortOptions
        )
        spinner.adapter = spinnerAdapter

        // Spinner에서 선택된 항목에 따라 데이터 업데이트
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> observeNearbyMemos()  // "내 주변" 선택 시
                    1 -> {
                        currentPage = 1 // 페이지 초기화
                        loadMemosSortedByDate(currentPage) // "최신순" 선택 시, 첫 페이지 로드
                    } // "최신순" 선택 시
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 선택되지 않았을 때 처리
            }
        }
    }
    // RecyclerView 설정 함수
    private fun setupRecyclerView() {
        memoAdapter = MemoAdapter(mutableListOf()) { memo ->
            showMemoBottomSheet(memo)  // 아이템 클릭 시 호출되는 콜백
        }
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) // 2열 그리드
            adapter = memoAdapter
        }
    }
    // "내 주변" 선택 시 ViewModel에서 데이터를 관찰하는 함수
    private fun observeNearbyMemos() {
        memoViewModel.nearbyMemoList.observe(viewLifecycleOwner, Observer { memos ->
            memoAdapter.updateMemoList(memos ?: mutableListOf())  // 데이터가 null일 경우 빈 리스트로 대체
        })
    }

    // 서버에서 최신 메모 데이터를 페이지별로 불러오는 메서드
    private fun loadMemosSortedByDate(page: Int) {
        if (isLoading) return  // 현재 로드 중일 경우 추가 요청을 하지 않음

        isLoading = true  // 로드 시작
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val emailIndex = G.emailIndex  // G 클래스에서 emailIndex 값을 가져옴
        Log.d("MylistFragment888", "Loading page $page with pageSize $pageSize")


        // 최신 메모를 페이지별로 불러오기 위한 Retrofit 호출
        val call = retrofitService.getMemosSortedByDate(pageSize, page, emailIndex!!.toInt())
        call.enqueue(object : Callback<List<MemoResponse>> {
            override fun onResponse(
                call: Call<List<MemoResponse>>,
                response: Response<List<MemoResponse>>
            ) {
                if (response.isSuccessful) {
                    val memos = response.body()
                    Log.d("MylistFragment888", "Successfully loaded ${memos?.size ?: 0} items")
                    if (page == 1) {
                        memoAdapter.updateMemoList(memos ?: mutableListOf())  // 첫 페이지는 초기화
                    } else {
                        memoAdapter.addMemoList(memos ?: mutableListOf())  // 추가 데이터는 리스트에 덧붙임
                    }
                } else {
                    Log.e("MylistFragment888", "Failed to load memos: ${response.errorBody()?.string()}")
                }
                isLoading = false  // 로드 종료
            }

            override fun onFailure(call: Call<List<MemoResponse>>, t: Throwable) {
                Log.e("MylistFragment888", "Network error: ${t.message}")
                // 네트워크 오류 처리
                isLoading = false  // 로드 종료
            }
        })
    }

    // RecyclerView 스크롤 리스너 설정 함수
    private fun setupRecyclerViewScrollListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // 스크롤이 멈췄을 때만 처리
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null)
                    val lastVisibleItem = lastVisibleItemPositions.maxOrNull() ?: 0

                    // 마지막 항목이 보이면 다음 페이지 데이터 요청
                    if (!isLoading && lastVisibleItem + 5 >= totalItemCount) {
                        currentPage++
                        loadMemosSortedByDate(currentPage)
                    }
                }
            }
        })
    }
    // 메모 클릭 시 BottomSheet를 표시하는 메서드
    private fun showMemoBottomSheet(memo: MemoResponse) {
        val addMemoFragment = AddmemoFragment()

        val bundle = Bundle().apply {
            putString("addressMemo", memo.addr_memo)
            putFloat("rating", memo.score_memo.toFloat())
            putString("memoText", memo.text_memo)
            putString("imageUrl", memo.img_memo)
            putString("x", memo.x_memo.toString())
            putString("y", memo.y_memo.toString())
            putString("dateMemo", memo.date_memo)
            putInt("id_memo", memo.id_memo.toInt())  // id_memo 추가
        }

        addMemoFragment.arguments = bundle
        addMemoFragment.show(childFragmentManager, "MemoBottomSheet")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}