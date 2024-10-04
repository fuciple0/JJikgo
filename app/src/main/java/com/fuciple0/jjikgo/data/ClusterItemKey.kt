package com.fuciple0.jjikgo.data

import com.naver.maps.map.clustering.ClusteringKey
import com.naver.maps.geometry.LatLng

class ClusterItemKey(private val memo: MemoResponse, private val position: LatLng) : ClusteringKey {
    override fun getPosition(): LatLng = position

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val itemKey = other as ClusterItemKey
        return memo.id_memo == itemKey.memo.id_memo
    }

    override fun hashCode(): Int {
        return memo.id_memo.hashCode()
    }
}
