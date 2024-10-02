package com.fuciple0.jjikgo.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.fuciple0.jjikgo.G
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.CircleImageTransform
import com.fuciple0.jjikgo.data.MemoDatabaseHelper
import com.fuciple0.jjikgo.databinding.FragmentLocationBinding
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

class LocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var dbHelper: MemoDatabaseHelper
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
        dbHelper = MemoDatabaseHelper(requireContext())

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


    // 네이버 맵이 준비되면 호출되는 콜백 함수
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = false

        checkLocationPermissionAndUpdateLocation()
        loadSavedMemos()
        setupMapClickListener()
    }

    private fun loadSavedMemos() {
        val savedMemos = dbHelper.getAllMemos()
        savedMemos.forEach { memo ->
            addMarkerForMemo(memo)
        }
    }

    private fun addMarkerForMemo(memo: MemoDatabaseHelper.Memo) {
        val coord = LatLng(memo.y.toDouble(), memo.x.toDouble())
        val marker = Marker().apply {
            position = coord
            icon = MarkerIcons.BLACK
            map = naverMap
        }

        // BLOB을 Bitmap으로 변환 (null 체크 포함)
        val bitmap = memo.imageBlob?.let { getBitmapFromBlob(it) } ?: getDefaultImageBlobBitmap() // 기본 이미지로 대체
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 85, 85, false)

        marker.icon = OverlayImage.fromBitmap(resizedBitmap)

        // Glide를 사용하여 원형 이미지를 생성
        Glide.with(this)
            .asBitmap()
            .load(resizedBitmap)
            .transform(CircleImageTransform())
            .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                    marker.icon = OverlayImage.fromBitmap(resource)
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
            })
    }

    // 기본 이미지의 Bitmap 반환
    private fun getDefaultImageBlobBitmap(): Bitmap {
        return BitmapFactory.decodeResource(resources, R.drawable.no_image)
    }

    // BLOB을 Bitmap으로 변환하는 함수
    private fun getBitmapFromBlob(blob: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(blob, 0, blob.size)
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

            Toast.makeText(context, "${G.userlocation}", Toast.LENGTH_SHORT).show()
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
}

