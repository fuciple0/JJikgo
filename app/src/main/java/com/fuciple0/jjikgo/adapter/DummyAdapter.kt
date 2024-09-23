package com.fuciple0.jjikgo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.fuciple0.jjikgo.R
import com.fuciple0.jjikgo.data.DummyItem
import com.fuciple0.jjikgo.databinding.RecyclerMyitemBinding
import com.fuciple0.jjikgo.fragments.MylistFragment

class DummyAdapter(val context: Context, var dummyList:MutableList<DummyItem>) : Adapter<DummyAdapter.VH>(){


    inner class  VH(var binding:RecyclerMyitemBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RecyclerMyitemBinding.inflate(LayoutInflater.from(context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int {
        return dummyList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item:DummyItem = dummyList[position]

        holder.binding.tvTitle.text = item.title
        holder.binding.tvBody.text = item.body
        holder.binding.tvRating.text = item.rating.toString()
        Glide.with(context).load(item.imgID).into(holder.binding.iv)

    }


}