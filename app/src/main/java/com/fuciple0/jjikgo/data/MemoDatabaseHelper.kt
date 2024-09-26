package com.fuciple0.jjikgo.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class MemoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "jjikgo.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_NAME = "mono_new"

        private const val COLUMN_ID = "id"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_RATING = "rating"
        private const val COLUMN_IMAGE_PATH = "image_path"
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
                $COLUMN_IMAGE_PATH TEXT,
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

    // 메모 저장 메서드에 날짜 및 시간 추가
    fun insertMemo(address: String, rating: Float, imagePath: String, memoText: String, x: String?, y: String?, dateTime: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ADDRESS, address)
            put(COLUMN_RATING, rating)
            put(COLUMN_IMAGE_PATH, imagePath)
            put(COLUMN_MEMO_TEXT, memoText)
            put(COLUMN_X, x)
            put(COLUMN_Y, y)
            put(COLUMN_DATE_TIME, dateTime) // 날짜 및 시간 추가
        }
        return db.insert(TABLE_NAME, null, values)
    }


    // MemoDatabaseHelper에서 저장된 모든 메모를 가져오는 메서드 추가
    // MemoDatabaseHelper.kt

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
                val imagePath = it.getString(it.getColumnIndexOrThrow(COLUMN_IMAGE_PATH))
                val memoText = it.getString(it.getColumnIndexOrThrow(COLUMN_MEMO_TEXT))
                val x = it.getString(it.getColumnIndexOrThrow(COLUMN_X))
                val y = it.getString(it.getColumnIndexOrThrow(COLUMN_Y))
                val dateTime = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE_TIME))

                val memo = Memo(id, address, rating, imagePath, memoText, x, y, dateTime)
                memoList.add(memo)
            }
        }
        return memoList
    }


    // Memo 데이터 클래스에 날짜 및 시간 추가
    data class Memo(
        val id: Long,
        val address: String,
        val rating: Float,
        val imagePath: String,
        val memo: String,
        val x: String,
        val y: String,
        val dateTime: String // 날짜 및 시간 추가
    )
}
