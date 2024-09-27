package com.fuciple0.jjikgo.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class MemoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "jjikgo.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_NAME = "mono_new"

        private const val COLUMN_ID = "id"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_RATING = "rating"
        private const val COLUMN_IMAGE_BLOB = "image_blob" // 이미지 BLOB 컬럼으로 수정
        private const val COLUMN_MEMO_TEXT = "memo_text"
        private const val COLUMN_X = "x" // 추가된 x 좌표
        private const val COLUMN_Y = "y" // 추가된 y 좌표
        private const val COLUMN_DATE_TIME = "date_time" // 추가된 날짜 및 시간
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_RATING REAL,
                $COLUMN_IMAGE_BLOB BLOB, -- BLOB으로 변경
                $COLUMN_MEMO_TEXT TEXT,
                $COLUMN_X TEXT, -- x 좌표 추가
                $COLUMN_Y TEXT, -- y 좌표 추가
                $COLUMN_DATE_TIME TEXT -- 날짜 및 시간 추가
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    // 메모 저장 메서드 - 이미지 바이트 배열을 저장
    fun insertMemo(address: String, rating: Float, imageBytes: ByteArray?, memoText: String, x: String?, y: String?, dateTime: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ADDRESS, address)
            put(COLUMN_RATING, rating)
            put(COLUMN_IMAGE_BLOB, imageBytes) // BLOB 데이터로 저장
            put(COLUMN_MEMO_TEXT, memoText)
            put(COLUMN_X, x)
            put(COLUMN_Y, y)
            put(COLUMN_DATE_TIME, dateTime)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // 저장된 모든 메모를 가져오는 메서드 - BLOB 형식의 이미지 데이터를 바이트 배열로 가져옴
    fun getAllMemos(): List<Memo> {
        val memoList = mutableListOf<Memo>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE_TIME DESC" // 날짜 및 시간으로 내림차순 정렬
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID))
                val address = it.getString(it.getColumnIndexOrThrow(COLUMN_ADDRESS))
                val rating = it.getFloat(it.getColumnIndexOrThrow(COLUMN_RATING))
                val imageBlob = it.getBlob(it.getColumnIndexOrThrow(COLUMN_IMAGE_BLOB)) // BLOB 데이터 가져오기
                val memoText = it.getString(it.getColumnIndexOrThrow(COLUMN_MEMO_TEXT))
                val x = it.getString(it.getColumnIndexOrThrow(COLUMN_X))
                val y = it.getString(it.getColumnIndexOrThrow(COLUMN_Y))
                val dateTime = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE_TIME))

                // Memo 객체로 변환
                val memo = Memo(id, address, rating, imageBlob, memoText, x, y, dateTime)
                memoList.add(memo)
            }
        }
        return memoList
    }

    // Memo 데이터 클래스에 이미지 BLOB 필드를 추가
    data class Memo(
        val id: Long,
        val address: String,
        val rating: Float,
        val imageBlob: ByteArray?, // 이미지 BLOB 추가
        val memo: String,
        val x: String,
        val y: String,
        val dateTime: String
    )

    // BLOB 데이터를 Bitmap으로 변환하는 메서드 (필요 시 활용)
    fun blobToBitmap(blob: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(blob, 0, blob.size)
    }
}
