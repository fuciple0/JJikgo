package com.fuciple0.jjikgo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MemoViewModel : ViewModel() {
    val memoList = MutableLiveData<List<MemoResponse>>()
}
