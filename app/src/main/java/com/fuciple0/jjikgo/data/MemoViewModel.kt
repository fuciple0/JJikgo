package com.fuciple0.jjikgo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MemoViewModel : ViewModel() {
    val nearbyMemoList = MutableLiveData<List<MemoResponse>>()  // 주변 메모 리스트
    val allMemoList = MutableLiveData<List<MemoResponse>>()     // 최신 메모 리스트
}
