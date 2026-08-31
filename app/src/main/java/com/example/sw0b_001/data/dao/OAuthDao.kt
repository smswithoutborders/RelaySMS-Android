package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.sw0b_001.data.OAuth

@Dao
interface OAuthDao {
    @Insert
    fun _insert(oAuth: OAuth)

    @Transaction
    fun insert(oAuth: OAuth) {
        clear()
        _insert(oAuth)
    }

    @Delete
    fun remove(oAuth: OAuth)
    @Query("delete from OAuth")
    fun clear()

    @Query("select * from OAuth where platformName = :platformName")
    fun fetch(platformName: String): OAuth?
}