package com.fuciple0.jjikgo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.databinding.FragmentLocationBinding
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap

class LocationFragment : Fragment() {

    lateinit var binding : FragmentLocationBinding
    lateinit var naverMap: NaverMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationBinding.inflate(inflater,container, false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MapFragment를 다른 프래그먼트 내에 배치하는 예제
//        val fm = childFragmentManager
//        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
//            ?: MapFragment.newInstance().also {
//                fm.beginTransaction().add(R.id.map, it).commit()
//            }





        binding.addMemoFab.setOnClickListener { showAddMemoBottomSheet() }

    }

    private fun showAddMemoBottomSheet() {
        val addMemoFragment = AddmemoFragment()
        addMemoFragment.show(childFragmentManager,"AddMemoBottomSheet")
    }



}