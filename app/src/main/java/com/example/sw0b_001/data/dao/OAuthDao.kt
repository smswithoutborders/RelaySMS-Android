package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.sw0b_001.data.OAuth

@Dao
interface OAuthDao {
    @Insert
    fun insert(oAuth: OAuth)
}