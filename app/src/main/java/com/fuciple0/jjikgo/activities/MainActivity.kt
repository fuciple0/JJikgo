package com.fuciple0.jjikgo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.databinding.ActivityMainBinding
import com.fuciple0.jjikgo.fragments.MylistFragment
import com.fuciple0.jjikgo.fragments.MypageFragment
import com.fuciple0.jjikgo.fragments.YourlistFragment
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk

class MainActivity : AppCompatActivity() {
    lateinit var naverMap: NaverMap
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val mapFragment: MapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync { naverMap ->
            this.naverMap = naverMap
        }

        binding.bnv.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bnv_menu_location -> {
                    // MainActivity로 이동 (현재 Activity 재시작)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish() // 현재 Activity 종료
                }

                R.id.bnv_menu_list -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MylistFragment()).commit()
                R.id.bnv_menu_shared -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, YourlistFragment()).commit()
                R.id.bnv_menu_account -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MypageFragment()).commit()
            }
            true
        }


    }
}