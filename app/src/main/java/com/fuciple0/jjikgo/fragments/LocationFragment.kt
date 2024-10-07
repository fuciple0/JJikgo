package com.fuciple0.jjikgo.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.CircleImageTransform
import com.fuciple0.jjikgo.data.ClusterItemKey
import com.fuciple0.jjikgo.data.MemoDatabaseHelper
import com.fuciple0.jjikgo.data.MemoResponse
import com.fuciple0.jjikgo.data.MemoViewModel
import com.fuciple0.jjikgo.databinding.FragmentLocationBinding
import com.fuciple0.jjikgo.network.RetrofitHelper
import com.fuciple0.jjikgo.network.RetrofitService
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

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

    private var lastCameraPosition: LatLng? = null
    private val MIN_DISTANCE_THRESHOLD = 200 // 최소 거리 임계값 (미터)

    // 마커를 관리할 리스트
    private val markers = mutableListOf<Marker>()
    // 클러스터링
    private var clusterManager: Clusterer<ClusterItemKey>? = null
    private var isClusteringEnabled = false
    // 줌레벨
    private var lastZoomLevel: Double? = null
    // 메모 뷰모델
    private val memoViewModel: MemoViewModel by activityViewModels()
    // 검색 장소명
    var searchQuery:String= ""
    var mylocationresult:LatLng? = null


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

        // 전달된 Bundle 값에서 좌표 정보 가져오기
        val longitude = arguments?.getDouble("longitude", 0.0) ?: 0.0
        val latitude = arguments?.getDouble("latitude", 0.0) ?: 0.0

        if (longitude != 0.0 && latitude != 0.0) {
            mylocationresult = LatLng(latitude, longitude)  // Bundle로 전달된 좌표를 mylocationresult 변수에 저장
        }

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
        // 상단 Edit텍스트박스 클릭 리스너
        binding.input.setOnClickListener {
            // LocalSearchFragment 인스턴스 생성
            val localSearchFragment = LocalSearchFragment()
            // Bundle을 사용해 mylocation 값을 전달
            val bundle = Bundle().apply {
                putDouble("longitude", G.mylocation?.longitude ?: 0.0)
                putDouble("latitude", G.mylocation?.latitude ?: 0.0)
            }
            localSearchFragment.arguments = bundle
            // FragmentManager를 통해 새로운 프래그먼트로 이동
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, localSearchFragment) // fragment_container는 현재 프래그먼트를 담고 있는 레이아웃의 ID입니다.
                .addToBackStack(null)  // 뒤로 가기 버튼을 눌렀을 때 이전 프래그먼트로 돌아가기 위해 추가
                .commit()
        }

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
            // "대한민국"을 제거하고 나머지 주소만 저장
            addressList[0].getAddressLine(0).replace("대한민국", "").trim()
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
        G.emailIndex = sharedPreferences.getInt("email_index", -1).toString()
        Log.d("LocationFragment887", "onMapReady Response: $G.emailIndex")

        if (G.emailIndex == "-1") {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // mylocationresult 값이 있으면 검색 결과에 기반하여 카메라 이동 및 마커 추가
        if (mylocationresult != null) {
            mylocationresult?.let {
                val cameraUpdate = CameraUpdate.scrollTo(it)
                naverMap.moveCamera(cameraUpdate)

                // 마커를 추가
                val marker = Marker().apply {
                    position = it
                    icon = MarkerIcons.GREEN
                    map = naverMap
                }
                currentLocationMarker = marker
                updateAddressMemo(mylocationresult!!)
            }
        } else {
            // mylocationresult 값이 없으면 현재 위치를 갱신하는 메서드 실행
            checkLocationPermissionAndUpdateLocation()
        }

        // 클러스터 매니저 초기화
        clusterManager = Clusterer.ComplexBuilder<ClusterItemKey>().build()


        // 카메라가 멈췄을 때 (지도가 완전히 이동을 끝낸 후) 호출
        naverMap.addOnCameraIdleListener {
            val cameraPosition = naverMap.cameraPosition.target
            val zoomLevel = naverMap.cameraPosition.zoom

            if (shouldUpdateMarkers(cameraPosition)) {
                if (zoomLevel <= 12) {
                    enableClustering()  // 줌 레벨이 12 이하이면 클러스터링 활성화
                } else {
                    disableClustering()  // 줌 레벨이 13 이상이면 개별 마커 표시
                }
                updateMarkersBasedOnCameraPosition()
                lastCameraPosition = cameraPosition  // 현재 카메라 위치 저장
            }
        }


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

                // InfoWindow 생성 및 마커에 연결
                val infoWindow = InfoWindow().apply {
                    adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
                        override fun getText(infoWindow: InfoWindow): CharSequence {
                            return "새 메모"
                        }
                    }
                }
                // currentLocationMarker가 null이 아닌 경우 InfoWindow를 연결
                currentLocationMarker?.let { marker ->
                    infoWindow.open(marker)  // InfoWindow를 마커에 연결
                }

                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng))
                updateAddressMemo(currentLatLng)
                G.mylocation = currentLatLng

                Toast.makeText(context, "현재 위치 - 위도: ${currentLatLng.latitude}, 경도: ${currentLatLng.longitude}", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(context, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 마커에 이미지를 설정하는 최적화된 메서드
    private fun addMarkerForMemo(memo: MemoResponse) {
        val coord = LatLng(memo.x_memo.toDouble(), memo.y_memo.toDouble())
        val marker = Marker().apply {
            position = coord
            icon = MarkerIcons.BLACK  // 기본 아이콘 설정
            map = naverMap
        }
        // 마커를 리스트에 추가
        markers.add(marker)

        // 날짜 캡션 설정
        val formattedDate = formatMemoDate(memo.date_memo)
        if (formattedDate.isNotEmpty()) {
            marker.captionText = formattedDate
            marker.captionRequestedWidth = 200  // 캡션 너비 설정
            marker.captionColor = Color.GRAY
        }

        // 공유된 메모인 경우 보조 캡션 추가
        if (memo.share_memo == 1) {
            marker.subCaptionText = "공유됨"
            marker.subCaptionRequestedWidth = 200  // 보조 캡션 너비 설정
            marker.subCaptionColor = Color.rgb(43, 92, 191)
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
            putString("x", memo.x_memo.toString())
            putString("y", memo.y_memo.toString())
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

    //사용자가 지도에서 임의의 위치를 클릭하면 마커 생성
    private fun setupMapClickListener() {
        naverMap.setOnMapClickListener { _, coord ->
            // 기존 마커 제거
            currentLocationMarker?.map = null
            userMarker?.map = null

            // 터치한 위치에 새로운 마커 추가
            userMarker = Marker().apply {
                position = coord
                icon = MarkerIcons.RED
                map = naverMap
            }
            // InfoWindow 생성 및 마커에 연결
            val infoWindow = InfoWindow().apply {
                adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return "새 메모"
                    }
                }
            }
            // userMarker가 null이 아닌 경우 InfoWindow를 연결
            userMarker?.let { marker ->
                infoWindow.open(marker)  // InfoWindow를 마커에 연결
            }
            //Toast.makeText(context, "터치한 위치 - 위도: ${coord.latitude}, 경도: ${coord.longitude}", Toast.LENGTH_SHORT).show()
            naverMap.moveCamera(CameraUpdate.scrollTo(coord))
            updateAddressMemo(coord)
        }
    }


    // 날짜 형식을 '24.10.4'로 변환하는 함수
    private fun formatMemoDate(dateMemo: String?): String {
        if (dateMemo.isNullOrEmpty()) return ""

        // 날짜 형식 변환 (예: "2024-10-04" -> "24.10.4")
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("yy.MM.d", Locale.getDefault())

        return try {
            val date = inputFormat.parse(dateMemo)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }


    // 두 위치 사이의 거리가 일정 거리 이상이거나 줌 레벨이 변화한 경우에만 업데이트 수행
    private fun shouldUpdateMarkers(newPosition: LatLng): Boolean {
        val currentZoomLevel = naverMap.cameraPosition.zoom
        lastCameraPosition?.let { lastPosition ->
            // 두 좌표 간의 거리 계산
            val results = FloatArray(1)
            Location.distanceBetween(
                lastPosition.latitude, lastPosition.longitude,
                newPosition.latitude, newPosition.longitude,
                results
            )
            val distance = results[0]
            Log.d("LocationFragment777", "Distance between positions: $distance meters")

            // 이동 거리가 임계값 이상이거나 줌 레벨이 변경된 경우
            if (distance > MIN_DISTANCE_THRESHOLD || lastZoomLevel != currentZoomLevel) {
                lastZoomLevel = currentZoomLevel  // 줌 레벨 업데이트
                return true  // 업데이트 수행
            }
            return false
        }

        // lastCameraPosition이 null인 경우, 업데이트 수행
        lastZoomLevel = currentZoomLevel
        return true
    }

    // 카메라 위치와 줌 레벨을 기반으로 마커 업데이트
    private fun updateMarkersBasedOnCameraPosition() {
        val cameraPosition = naverMap.cameraPosition
        val zoomLevel = cameraPosition.zoom
        val centerLatLng = cameraPosition.target

        val visibleRegion = naverMap.contentBounds

        // 현재 화면의 경계 좌표를 가져옴
        val southwest = visibleRegion.southWest
        val northeast = visibleRegion.northEast

        // 로그 출력 (카메라 위치, 줌 레벨, 경계 좌표 등)
        Log.d("LocationFragment777", "Camera position: Lat = ${centerLatLng.latitude}, Lng = ${centerLatLng.longitude}")
        Log.d("LocationFragment777", "Zoom level: $zoomLevel")
        Log.d("LocationFragment777", "Southwest: Lat = ${southwest.latitude}, Lng = ${southwest.longitude}")
        Log.d("LocationFragment777", "Northeast: Lat = ${northeast.latitude}, Lng = ${northeast.longitude}")

        // 데이터베이스 쿼리를 수정하여 현재 경계 안에 있는 데이터만 불러옴
        loadNearbyMemos(centerLatLng, zoomLevel, southwest, northeast)
    }


    // 카메라 위치와 줌 레벨을 기반으로 범위 내의 메모를 불러오는 메서드
    private fun loadNearbyMemos(center: LatLng, zoomLevel: Double, southwest: LatLng, northeast: LatLng) {

        val retrofit = RetrofitHelper.getRetrofitInstance("http://fuciple0.dothome.co.kr/")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        // Retrofit 호출: 범위 내 메모 가져오기
        val call = retrofitService.getMemosInBounds(
            center.latitude, center.longitude,
            southwest.latitude, southwest.longitude,
            northeast.latitude, northeast.longitude,
            G.emailIndex!!.toInt()  // emailIndex 전달
        )

        call.enqueue(object : Callback<List<MemoResponse>> {
            override fun onResponse(call: Call<List<MemoResponse>>, response: Response<List<MemoResponse>>) {
                if (response.isSuccessful) {
                    val memos = response.body()
                    Log.d("LocationFragment887", "Memo Response: $memos")
                    Log.d("LocationFragment887", "Number of memos retrieved: ${memos?.size ?: 0}")

                    // ViewModel에 데이터 저장 (주변 메모 리스트)
                    memoViewModel.nearbyMemoList.postValue(memos)

                    if (isClusteringEnabled) {
                        memos?.forEach { memo ->
                            addClusterItem(memo)  // 클러스터 아이템 추가
                        }
                        clusterManager?.map = naverMap
                    } else {
                        clearExistingMarkers()
                        memos?.forEach { memo ->
                            addMarkerForMemo(memo)  // 개별 마커 추가
                        }
                    }
                } else {
                    Log.e("LocationFragment887", "Failed to load memos: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<MemoResponse>>, t: Throwable) {
                Log.e("LocationFragment777", "Network error: ${t.message}")
            }
        })
    }



    // 기존 마커들을 제거하는 메서드
    private fun clearExistingMarkers() {
        for (marker in markers) {
            marker.map = null  // 지도에서 마커 제거
        }
        markers.clear()  // 리스트 초기화
    }

    private val clusterItems = mutableListOf<ClusterItemKey>()


    private fun enableClustering() {
        if (!isClusteringEnabled) {
            clearExistingMarkers()  // 클러스터링 시작 시 기존 마커 제거
            clusterManager?.removeAll(clusterItems)  // 클러스터 항목 모두 제거
            isClusteringEnabled = true
        }
    }

    private fun disableClustering() {
        if (isClusteringEnabled) {
            clusterManager?.removeAll(clusterItems)  // 클러스터 항목 삭제
            isClusteringEnabled = false
        }
    }

    private fun addClusterItem(memo: MemoResponse) {
        val key = ClusterItemKey(memo, LatLng(memo.x_memo.toDouble(), memo.y_memo.toDouble()))
        clusterItems.add(key)
        clusterManager?.add(key, memo)
    }




}

