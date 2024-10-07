package com.fuciple0.jjikgo.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.adapter.SharedMemoAdapter
import com.fuciple0.jjikgo.data.SharedMemoData
import com.fuciple0.jjikgo.databinding.FragmentYourlistBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YourlistFragment : Fragment() {

    private var _binding: FragmentYourlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedMemoAdapter: SharedMemoAdapter
    private lateinit var memoList: List<SharedMemoData>  // memoList를 클래스 변수로 저장
    private var isMapVisible = false // 맵 가시성 상태
    private lateinit var naverMap: NaverMap  // NaverMap 객체 저장
    private val markers = mutableListOf<Marker>()  // 마커 리스트

    private var isLoading = false  // 로딩 상태 확인
    private var currentPage = 1    // 현재 페이지
    private val pageSize = 16      // 한 번에 불러올 데이터 개수
    private var currentRadius = 50.0  // 기본 범위 50km

    // 정렬 방식에 따른 메서드 호출을 결정하는 변수
    private var currentSortOption = 0 // 0: 위치 기준, 1: 최신순, 2: 평점 높은 순

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYourlistBinding.inflate(inflater, container, false)
        val view = binding.root

        // RecyclerView 설정
        setupRecyclerView()

        // RecyclerView 스크롤 리스너 설정
        setupRecyclerViewScrollListener()

        // 초기 실행 메소드 : 내 주변 검색
        loadMemosByLocation()

        // 스피너 설정
        setupSpinner()


        // 지도보기 버튼 클릭 시 처리
        binding.showMapFab.setOnClickListener {
            if (!isMapVisible) {
                showMapWithMarkers(memoList)
                isMapVisible = true
                toggleMapButton(true)  // 버튼 스타일 변경
            } else {
                hideMap()
                isMapVisible = false
                toggleMapButton(false)  // 버튼 스타일 원래대로 변경
            }
        }
        return view
    }
    // RecyclerView 스크롤 리스너 설정 함수
    private fun setupRecyclerViewScrollListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // 스크롤이 멈췄을 때만 처리
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    // 마지막 항목이 보이면 다음 페이지 데이터 요청
                    if (!isLoading && lastVisibleItemPosition + 5 >= totalItemCount) {
                        currentPage++
                        when (currentSortOption) {
                            0 -> loadMemosByLocation()
                            1 -> loadMemosSortedByDate(currentPage)
                            2 -> loadMemosByRating(currentPage)
                        }
                    }
                }
            }
        })
    }

    // 지도에 있는 마커들을 모두 삭제하는 함수
    private fun clearMarkers() {
        for ((index, marker) in markers.withIndex()) {
            marker.map = null  // 지도에서 마커를 제거
            }
        markers.clear()  // 마커 리스트 초기화
        }


    // 새로운 마커를 추가하는 함수
    private fun updateMarkers(memoList: List<SharedMemoData>) {
        clearMarkers()  // 기존 마커 삭제

        // memoList의 크기 및 각 메모의 값을 로그로 출력
        Log.d("YourlistFragment43", "updateMarkers - Number of memos: ${memoList.size}")

        for (memo in memoList) {
            Log.d("YourlistFragment43", "Memo Details: nickname=${memo.nickname}, latitude=${memo.latitude}, longitude=${memo.longitude}, userProfile=${memo.userProfile}")
            // addMarkerForMemo 호출하여 프로필 이미지 및 닉네임 설정
            addMarkerForMemo(memo)  // 프로필 이미지 및 닉네임 설정하는 메서드 호출
        }
    }


    // 마커를 프로필 이미지로 설정하고, 캡션은 닉네임으로 설정하는 함수
    private fun addMarkerForMemo(memo: SharedMemoData) {
        val coord = LatLng(memo.latitude, memo.longitude)
        val marker = Marker().apply {
            position = coord
            map = naverMap
        }
        // 닉네임을 캡션으로 설정
        marker.captionText = memo.nickname
        marker.captionRequestedWidth = 200
        marker.captionColor = Color.GRAY

        // Glide로 프로필 이미지를 로드하여 마커에 설정
        if (memo.userProfile != null && memo.userProfile.isNotEmpty()) {
            val fullImageUrl = "http://fuciple0.dothome.co.kr/Jjikgo/${memo.userProfile}"

            Glide.with(this)
                .asBitmap()
                .load(fullImageUrl)
                .apply(
                    RequestOptions()
                        .circleCrop()  // 원형으로 변환
                        .diskCacheStrategy(DiskCacheStrategy.ALL)  // 모든 이미지 캐시
                        .override(85, 85)  // 비트맵 크기 조정
                )
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        marker.icon = OverlayImage.fromBitmap(resource)  // 마커에 이미지 설정
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 필요 시 처리
                    }
                })
        } else {
            // 프로필 이미지가 없을 경우 기본 마커 설정
            val defaultBitmap = BitmapFactory.decodeResource(resources, R.drawable.default_marker)
            val resizedBitmap = Bitmap.createScaledBitmap(defaultBitmap, 85, 85, false)
            marker.icon = OverlayImage.fromBitmap(resizedBitmap)  // 기본 마커 설정
        }
        // 마커 리스트에 추가
        markers.add(marker)
    }

    // 맵을 보여주면서 마커를 찍는 함수
    private fun showMapWithMarkers(memoList: List<SharedMemoData>) {
        // 지도 프래그먼트를 표시
        binding.mapFragment.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        // NaverMap 객체 가져오기
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync { naverMap ->
            this.naverMap = naverMap  // 네이버 맵 객체 저장
            // 기존 마커 삭제
            clearMarkers()
            // 메모 리스트의 각 메모에 대해 마커 추가
            for (memo in memoList) {
                addMarkerForMemo(memo)  // 각 메모에 대해 마커 추가
            }
            // 최신 데이터로 카메라 이동 (줌 레벨 조정)
            val latestMemo = memoList.firstOrNull()  // 가장 최신 데이터
            latestMemo?.let {
                val latestLatLng = LatLng(it.latitude, it.longitude)
                val cameraUpdate = CameraUpdate.scrollAndZoomTo(latestLatLng, naverMap.cameraPosition.zoom - 1)  // 줌 레벨을 1단계 넓게
                    .animate(CameraAnimation.Fly)
                naverMap.moveCamera(cameraUpdate)
            }
        }
    }

    // 맵 숨기기 함수
    private fun hideMap() {
        binding.mapFragment.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    // 버튼 스타일 변경 함수
    private fun toggleMapButton(showingMap: Boolean) {
        if (showingMap) {
            binding.showMapFab.text = "목록보기"
            binding.showMapFab.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.showMapFab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.showMapFab.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.gray)  // 테두리 설정
        } else {
            binding.showMapFab.text = "지도보기"
            binding.showMapFab.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pointcolor))
            binding.showMapFab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    // RecyclerView 설정 함수
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    // 스피너 설정 함수
    private fun setupSpinner() {
        val spinner: Spinner = binding.materialToolbar.findViewById(R.id.toolbar_spinner)
        val sortOptions = resources.getStringArray(R.array.share_items)
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
                    0 -> {
                        currentPage = 1
                        loadMemosByLocation()
                    }
                    1 -> {
                        currentPage = 1
                        loadMemosSortedByDate(currentPage)
                    }
                    2 -> {
                        currentPage = 1
                        loadMemosByRating(currentPage)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // 선택되지 않았을 때 처리
            }
        }
    }

    // 서버에서 내 주변 기준으로 데이터를 불러오는 메서드
    private fun loadMemosByLocation() {
        if (isLoading) return
        isLoading = true

        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val latitude = G.mylocation?.latitude ?: 0.0
        val longitude = G.mylocation?.longitude ?: 0.0

        val call = retrofitService.getMemosByLocation(latitude, longitude, pageSize, currentPage)
        call.enqueue(object : Callback<List<SharedMemoData>> {  // 타입을 SharedMemoData로 변경
            override fun onResponse(call: Call<List<SharedMemoData>>, response: Response<List<SharedMemoData>>) {
                if (response.isSuccessful) {
                    memoList = response.body() ?: emptyList()  // memoList 업데이트
                    memoList.let {
                        Log.d("YourlistFragment78", "50km_총 가져온 메모 수: ${memoList.size}")
                        updateRecyclerView(it)
                        if (isMapVisible) {
                            updateMarkers(it)  // 지도 상태일 때 마커 업데이트
                        }
                    }
                } else {
                    Log.e("YourlistFragment", "Failed to load nearby memos: ${response.errorBody()?.string()}")
                }
                isLoading = false
            }

            override fun onFailure(call: Call<List<SharedMemoData>>, t: Throwable) {
                Log.e("YourlistFragment", "Network error: ${t.message}")
                isLoading = false
            }
        })
    }


    // 최신순으로 메모를 불러오는 메서드
    private fun loadMemosSortedByDate(page: Int) {
        if (isLoading) return
        isLoading = true

        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val call = retrofitService.getMemosSortedByLatest(pageSize, page)
        call.enqueue(object : Callback<List<SharedMemoData>> {
            override fun onResponse(call: Call<List<SharedMemoData>>, response: Response<List<SharedMemoData>>) {
                if (response.isSuccessful) {
                    memoList = response.body() ?: emptyList()  // memoList 업데이트
                    memoList.let {
                        Log.d("YourlistFragment78", "Latest_총 가져온 메모 수: ${memoList.size}")
                        updateRecyclerView(it)
                        if (isMapVisible) {
                            updateMarkers(it)  // 지도 상태일 때 마커 업데이트
                        }
                    }
                } else {
                    Log.e("YourlistFragment", "Failed to load shared memos: ${response.errorBody()?.string()}")
                }
                isLoading = false
            }

            override fun onFailure(call: Call<List<SharedMemoData>>, t: Throwable) {
                Log.e("YourlistFragment", "Network error: ${t.message}")
                isLoading = false
            }
        })
    }

    // 평점 높은 순으로 메모를 불러오는 메서드
    private fun loadMemosByRating(page: Int) {
        if (isLoading) return
        isLoading = true

        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val call = retrofitService.getMemosSortedByRating(pageSize, page)
        call.enqueue(object : Callback<List<SharedMemoData>> {  // 타입을 SharedMemoData로 변경
            override fun onResponse(call: Call<List<SharedMemoData>>, response: Response<List<SharedMemoData>>) {
                if (response.isSuccessful) {
                    memoList = response.body() ?: emptyList()  // memoList 업데이트
                    memoList.let {
                        Log.d("YourlistFragment78", "Rating_총 가져온 메모 수: ${memoList.size}")
                        updateRecyclerView(it)
                        if (isMapVisible) {
                            updateMarkers(it)  // 지도 상태일 때 마커 업데이트
                        }
                    }
                } else {
                    Log.e("YourlistFragment", "Failed to load memos by rating: ${response.errorBody()?.string()}")
                }
                isLoading = false
            }
            override fun onFailure(call: Call<List<SharedMemoData>>, t: Throwable) {
                Log.e("YourlistFragment", "Network error: ${t.message}")
                isLoading = false
            }
        })
    }

    // 리사이클러뷰 업데이트 함수
    private fun updateRecyclerView(memoList: List<SharedMemoData>) {
        if (currentPage == 1) {
            sharedMemoAdapter = SharedMemoAdapter(memoList.toMutableList()) // List를 MutableList로 변환
            binding.recyclerView.adapter = sharedMemoAdapter
        } else {
            sharedMemoAdapter.addMemoList(memoList) // 기존 어댑터에 데이터 추가
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
