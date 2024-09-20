package com.fuciple0.jjikgo

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //카카오 SDK 초기화
        KakaoSdk.init(this, "fc2a68e80a52a25bb9510c8d81b0f53a")

    }
}