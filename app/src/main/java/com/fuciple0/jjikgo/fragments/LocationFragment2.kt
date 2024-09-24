package com.fuciple0.jjikgo.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.databinding.FragmentLocationBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource

class LocationFragment2 : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentLocationBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewBinding을 사용하여 레이아웃을 inflate
        binding = FragmentLocationBinding.inflate(inflater, container, false)

        // 지도 프래그먼트 로드
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }

        // 지도 비동기 로드
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FloatingActionButton 클릭 이벤트
        binding.addMemoFab.setOnClickListener { showAddMemoBottomSheet() }
    }

    private fun showAddMemoBottomSheet() {
        val addMemoFragment = AddmemoFragment()
        addMemoFragment.show(childFragmentManager, "AddMemoBottomSheet")
    }




    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.locationSource = locationSource

        val locationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true

        // 지도 UI 설정
        val uiSettings = naverMap.uiSettings
        uiSettings.isScaleBarEnabled = false
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLocationButtonEnabled = true



        //naverMap.locationTrackingMode = LocationTrackingMode.Follow

}


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
