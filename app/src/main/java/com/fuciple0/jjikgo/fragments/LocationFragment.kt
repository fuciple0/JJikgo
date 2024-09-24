package com.fuciple0.jjikgo.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.databinding.FragmentLocationBinding
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons

class LocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentLocationBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

    private var currentLocationMarker: Marker? = null
    private var userMarker: Marker? = null

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)

        // 플로팅 액션 버튼 클릭 리스너 설정
        binding.mylocationFab.setOnClickListener {
            checkLocationPermissionAndUpdateLocation()
        }

        return binding.root
    }

    // 네이버 맵이 준비되면 호출되는 콜백 함수
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        // UI 설정: 사용자 위치 버튼 비활성화
        naverMap.uiSettings.isLocationButtonEnabled = false

        // 처음 앱 실행 시 위치 권한을 확인하고 현재 위치로 지도 이동 및 마커 추가
        checkLocationPermissionAndUpdateLocation()

        // 사용자가 지도를 터치할 때 마커를 추가하는 리스너
        naverMap.setOnMapClickListener { _, coord ->
            // 현재 위치 마커 제거
            currentLocationMarker?.map = null

            // 이전에 추가된 마커 제거
            userMarker?.map = null

            // 터치한 위치에 새로운 마커 추가
            userMarker = Marker().apply {
                position = coord
                icon = MarkerIcons.RED
                map = naverMap
            }

            // 터치한 좌표를 Toast로 출력
            Toast.makeText(
                context,
                "터치한 위치 - 위도: ${coord.latitude}, 경도: ${coord.longitude}",
                Toast.LENGTH_SHORT
            ).show()

            // 터치한 위치로 카메라 이동
            naverMap.moveCamera(CameraUpdate.scrollTo(coord))
        }
    }

    // 위치 권한을 확인하고 업데이트하는 메서드
    private fun checkLocationPermissionAndUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 있는 경우 현재 위치를 갱신
            updateCurrentLocation()
        } else {
            // 권한이 없는 경우 권한을 요청
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // 현재 위치를 갱신하고 마커를 추가하는 메서드
    private fun updateCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                // 현재 위치에 마커 추가. 기존 마커가 있으면 제거
                currentLocationMarker?.map = null
                currentLocationMarker = Marker().apply {
                    position = currentLatLng
                    icon = MarkerIcons.GREEN
                    map = naverMap
                }

                // 사용자가 생성한 마커가 있으면 제거
                userMarker?.map = null
                userMarker = null // 마커 참조를 초기화

                // 현재 위치로 카메라 이동
                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng))

                // 좌표를 Toast로 출력
                Toast.makeText(
                    context,
                    "현재 위치 - 위도: ${currentLatLng.latitude}, 경도: ${currentLatLng.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    // 위치 권한 요청 결과를 처리하는 메서드
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허가된 경우 현재 위치를 갱신
                updateCurrentLocation()
            } else {
                // 권한이 거부된 경우 경고 메시지 출력
                Toast.makeText(context, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}

