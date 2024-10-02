package com.fuciple0.jjikgo.data

data class MemoResponse(
    val id_memo: Int,
    val addr_memo: String,
    val score_memo: Int,
    val img_memo: String?,  // 이미지가 없는 경우도 있으므로 nullable
    val text_memo: String,
    val x_memo: String,
    val y_memo: String,
    val date_memo: String,
    val share_memo: Int,  // Int로 받아서
    val email_index: Int
) {
    fun isShareMemo(): Boolean {
        return share_memo == 1  // 1이면 true, 0이면 false 반환
    }
}


