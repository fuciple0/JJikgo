package com.fuciple0.jjikgo.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.adapter.LocalListAdapter
import com.fuciple0.jjikgo.data.KakoSearchPlaceResponse
import com.fuciple0.jjikgo.data.Place
import com.fuciple0.jjikgo.data.PlaceMeta
import com.fuciple0.jjikgo.databinding.FragmentLocalsearchBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocalSearchFragment : Fragment() {

    private lateinit var binding: FragmentLocalsearchBinding
    private var longitude: Double? = null
    private var latitude: Double? = null
    private lateinit var searchQuery: String

    // 카카오 검색 결과(json)를 분석한 객체 참조변수
    var searchPlaceResponse : KakoSearchPlaceResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // View Binding 설정
        binding = FragmentLocalsearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 mylocation 값 확인
        longitude = arguments?.getDouble("longitude")
        latitude = arguments?.getDouble("latitude")

        // input EditText에 포커스 설정 및 키보드 띄우기
        binding.input.requestFocus()  // EditText에 포커스를 맞춤
        // 소프트 키보드 강제 호출
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.input, InputMethodManager.SHOW_IMPLICIT)

        // EditText에서 drawable (아이콘) 클릭 시 이벤트 처리
        binding.input.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.input.compoundDrawablesRelative[2] // 오른쪽 아이콘
                val drawableStart = binding.input.compoundDrawablesRelative[0] // 왼쪽 아이콘

                // 오른쪽의 돋보기 아이콘을 클릭한 경우
                if (drawableEnd != null && event.rawX >= (binding.input.right - binding.input.compoundPaddingEnd - drawableEnd.bounds.width())) {
                    searchQuery = binding.input.text.toString()
                    searchPlaces()  // 장소 검색 메서드 호출
                    return@setOnTouchListener true
                }

                // 왼쪽의 뒤로가기 아이콘을 클릭한 경우
                if (drawableStart != null && event.rawX <= (binding.input.left + binding.input.compoundPaddingStart + drawableStart.bounds.width())) {
                    // 뒤로 가기 처리
                    requireActivity().onBackPressed() // 또는 finish()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // 키보드의 검색 버튼 처리
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchQuery = binding.input.text.toString()
                searchPlaces()  // 장소 검색 메서드 호출
                true
            } else {
                false
            }
        }
    }

    // 카카오의 키워드 장소 검색 open API를 통해 장소정보들을 가져오는 작업 메소드
    private fun searchPlaces() {
        // 검색 요청 데이터 확인용 Toast
        Toast.makeText(requireContext(), "$searchQuery : $longitude, $latitude", Toast.LENGTH_SHORT).show()

        // Retrofit을 사용하여 Kakao API 호출
        val retrofit = RetrofitHelper.getRetrofitInstance("https://dapi.kakao.com")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        // Kakao 검색 API 호출
//        val call = retrofitService.searchPlacesFromServer(searchQuery, longitude.toString(), latitude.toString())
//        call.enqueue(object : Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                val s = response.body()
//                AlertDialog.Builder(requireContext()).setMessage(s).create().show()
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                Toast.makeText(requireContext(), "오류: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
        val call= retrofitService.searchPlacesFromServer2(searchQuery, longitude.toString(), latitude.toString())
        call.enqueue(object : Callback<KakoSearchPlaceResponse> {
            override fun onResponse(
                p0: Call<KakoSearchPlaceResponse>,
                p1: Response<KakoSearchPlaceResponse>
            ) {
                //p1파라미터로 전달된 json을 파싱한 결과 받기 - 그 결과를 fragment들에서 사용하기에 멤버변수로 참조하기
                searchPlaceResponse = p1.body()

                // 데이터가 온전히 분석되었는지 확인해보기..
                val meta:PlaceMeta? = searchPlaceResponse?.meta
                val documents:List<Place>? = searchPlaceResponse?.documents
                AlertDialog.Builder(requireContext()).setMessage("${meta?.total_count} \n ${documents?.size}").create().show()

                // 새로운 검색이 완료되면 무조건, List 탭을 가장 먼저 보여주도록..
                //binding.bnv.selectedItemId = R.id.bnv_menu_list

                // RecyclerView 레이아웃 매니저 설정
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                // RecyclerView에 어댑터 연결
                if (documents != null) {
                    val adapter = LocalListAdapter(requireContext(), documents)
                    binding.recyclerView.adapter = adapter
                }

            }

            override fun onFailure(p0: Call<KakoSearchPlaceResponse>, p1: Throwable) {
                Toast.makeText(requireContext(), "오류: ${p1.message}", Toast.LENGTH_SHORT).show()
            }
        })



    }


}
