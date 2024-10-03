package com.fuciple0.jjikgo.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.CircleImageTransform
import com.fuciple0.jjikgo.data.MemoDatabaseHelper
import com.fuciple0.jjikgo.data.MemoResponse
import com.fuciple0.jjikgo.databinding.FragmentLocationBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentLocationBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

    private var currentLocationMarker: Marker? = null
    private var userMarker: Marker? = null
    private var addressMemo: String = ""
    private var x: String = ""
    private var y: String = ""

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationBinding.inflate(inflater, container, false)

        setupMapFragment()
        setupClickListeners()

        return binding.root
    }

    private fun setupMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    private fun setupClickListeners() {
        binding.mylocationFab.setOnClickListener { checkLocationPermissionAndUpdateLocation() }
        binding.addMemoFab.setOnClickListener { showAddMemoBottomSheet() }
    }

    private fun showAddMemoBottomSheet() {
        val addMemoFragment = AddmemoFragment()
        val bundle = Bundle().apply {
            putString("addressMemo", addressMemo)
            putString("x", x)
            putString("y", y)
        }
        addMemoFragment.arguments = bundle
        addMemoFragment.show(childFragmentManager, "AddMemoBottomSheet")
    }

    // 좌표 기반으로 주소 가져와서 addressMemo 값 저장
    private fun updateAddressMemo(latLng: LatLng) {
        y = latLng.longitude.toString()
        x = latLng.latitude.toString()

        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        addressMemo = if (!addressList.isNullOrEmpty()) {
            addressList[0].getAddressLine(0)
        } else {
            "주소를 찾을 수 없습니다."
        }
        Log.i("addressMemo", addressMemo)
    }


    // 네이버 맵이 준비되면 호출되는 콜백 함수
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = false

        // SharedPreferences에서 emailIndex 값을 가져옴
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val emailIndex = sharedPreferences.getInt("email_index", -1).toString()

        if (emailIndex == "-1") {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        checkLocationPermissionAndUpdateLocation()

        loadMemosForUser(emailIndex.toInt())

        setupMapClickListener()
    }

    // 위치 권한을 확인하고 업데이트하는 메서드
    private fun checkLocationPermissionAndUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateCurrentLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // 현재 위치를 갱신하고 마커를 추가하는 메서드
    private fun updateCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // 메모바텀시트에서 메모를 입력하면, 메모입력지 기준으로 카메라 이동
        if(G.userlocation!=null) {
            naverMap.moveCamera(CameraUpdate.scrollTo(G.userlocation!!))
            G.userlocation = null // 다시 널값으로 만들어주기

            //Toast.makeText(context, "${G.userlocation}", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)

                // 현재 위치에 마커 추가
                currentLocationMarker?.map = null
                currentLocationMarker = Marker().apply {
                    position = currentLatLng
                    icon = MarkerIcons.GREEN
                    map = naverMap
                }
                userMarker?.map = null
                userMarker = null

                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng))
                updateAddressMemo(currentLatLng)

                Toast.makeText(context, "현재 위치 - 위도: ${currentLatLng.latitude}, 경도: ${currentLatLng.longitude}", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 서버에서 기록 불러오기
    private fun loadMemosForUser(emailIndex: Int) {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        val call = retrofitService.getMemos(emailIndex, 0)
        call.enqueue(object : Callback<List<MemoResponse>> {
            override fun onResponse(call: Call<List<MemoResponse>>, response: Response<List<MemoResponse>>) {
                if (response.isSuccessful) {
                    val memos = response.body()
                    // 서버에서 받아온 데이터 로그로 출력
                    Log.d("LocationFragment", "Received memos: $memos")

                    memos?.forEach { memo ->
                        // 각 메모 데이터 로그로 출력
                        Log.d("LocationFragment", "Memo ID: ${memo.id_memo}, Address: ${memo.addr_memo}, Score: ${memo.score_memo}")
                        addMarkerForMemo(memo)
                    }
                } else {
                    Log.e("LocationFragment", "Failed to load memos: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<MemoResponse>>, t: Throwable) {
                Log.e("LocationFragment", "Network error: ${t.message}")
            }
        })
    }


    // 마커에 이미지를 설정하는 최적화된 메서드
    private fun addMarkerForMemo(memo: MemoResponse) {
        val coord = LatLng(memo.x_memo.toDouble(), memo.y_memo.toDouble())
        val marker = Marker().apply {
            position = coord
            icon = MarkerIcons.BLACK  // 기본 아이콘 설정
            map = naverMap
        }

        // 마커 클릭 리스너 추가
        marker.setOnClickListener {
            showMemoBottomSheet(memo)
            true
        }

        // 이미지 주소가 있을 경우 Glide로 이미지 로드 및 원형 변환 적용
        if (memo.img_memo != null && memo.img_memo.isNotEmpty()) {
            val fullImageUrl = "http://fuciple0.dothome.co.kr/Jjikgo/${memo.img_memo}"

            Glide.with(this)
                .asBitmap()
                .load(fullImageUrl)
                .apply(
                    RequestOptions()
                        .circleCrop()  // 원형 변환
                        .diskCacheStrategy(DiskCacheStrategy.ALL)  // 모든 이미지 캐시
                        .override(85, 85))  // 비트맵 크기 조정
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        marker.icon = OverlayImage.fromBitmap(resource)  // 마커에 이미지 설정
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 필요 시 처리
                    }
                })
        } else {
            // 이미지가 없을 경우 기본 마커 이미지 설정
            val defaultBitmap = BitmapFactory.decodeResource(resources, R.drawable.default_marker)
            val resizedBitmap = Bitmap.createScaledBitmap(defaultBitmap, 85, 85, false)
            marker.icon = OverlayImage.fromBitmap(resizedBitmap)  // 기본 마커 이미지 설정
        }
    }

    private fun showMemoBottomSheet(memo: MemoResponse) {
        // AddmemoFragment 재활용
        val addMemoFragment = AddmemoFragment()

        val bundle = Bundle().apply {
            putString("addressMemo", memo.addr_memo)
            putFloat("rating", memo.score_memo.toFloat())
            putString("memoText", memo.text_memo)
            putString("imageUrl", memo.img_memo)
            putString("x", memo.x_memo)
            putString("y", memo.y_memo)
            putString("dateMemo", memo.date_memo)
            putInt("id_memo", memo.id_memo)  // id_memo 추가
        }

        addMemoFragment.arguments = bundle
        addMemoFragment.show(childFragmentManager, "MemoBottomSheet")
    }


    // 위치 권한 요청 결과를 처리하는 메서드
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateCurrentLocation()
            } else {
                Toast.makeText(context, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setupMapClickListener() {
        naverMap.setOnMapClickListener { _, coord ->
            currentLocationMarker?.map = null
            userMarker?.map = null

            userMarker = Marker().apply {
                position = coord
                icon = MarkerIcons.RED
                map = naverMap
            }

            Toast.makeText(context, "터치한 위치 - 위도: ${coord.latitude}, 경도: ${coord.longitude}", Toast.LENGTH_SHORT).show()
            naverMap.moveCamera(CameraUpdate.scrollTo(coord))
            updateAddressMemo(coord)
        }
    }



}

