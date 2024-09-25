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
import com.fuciple0.jjikgo.fragments.LocationFragment
import com.fuciple0.jjikgo.fragments.MylistFragment
import com.fuciple0.jjikgo.fragments.MypageFragment
import com.fuciple0.jjikgo.fragments.YourlistFragment
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //처음 보여질 Bottom 탭의  Fragment 붙이기
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, LocationFragment()).commit()

        binding.bnv.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bnv_menu_location -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, LocationFragment()).commit()
                R.id.bnv_menu_list -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MylistFragment()).commit()
                R.id.bnv_menu_shared -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, YourlistFragment()).commit()
                R.id.bnv_menu_account -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MypageFragment()).commit()
            }
            true
        }





    }
}