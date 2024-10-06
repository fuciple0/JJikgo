package com.fuciple0.jjikgo.adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.Place
import com.fuciple0.jjikgo.databinding.RecyclerLocalItemBinding
import com.fuciple0.jjikgo.fragments.LocationFragment

class LocalListAdapter (val context: Context, val document:List<Place>) : Adapter<LocalListAdapter.VH>(){

    inner class VH(var binding:RecyclerLocalItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RecyclerLocalItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int {
        return document.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val place:Place = document[position]

        holder.binding.tvPlaceName.text = place.place_name
        holder.binding.tvDistance.text = place.distance + "m"
        holder.binding.tvAddress.text = if(place.road_address_name == "") place.address_name else place.road_address_name

        // 클릭 이벤트 추가
        holder.itemView.setOnClickListener {
            // Place 정보와 함께 LocationFragment로 돌아가기
            val locationFragment = LocationFragment()

            // Bundle로 좌표 정보 전달
            val bundle = Bundle().apply {
                putDouble("longitude", place.x.toDouble())  // place.x와 place.y는 좌표 값 (String 타입일 수 있으니 변환)
                putDouble("latitude", place.y.toDouble())
                putString("place_name", place.place_name)  // 마커에 사용할 장소 이름
            }
            locationFragment.arguments = bundle

            // FragmentManager를 통해 LocationFragment로 이동
            (context as androidx.fragment.app.FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, locationFragment)
                .addToBackStack(null)  // 뒤로 가기 버튼을 눌렀을 때 이전 프래그먼트로 돌아가기 위해 추가
                .commit()
        }


    }




}