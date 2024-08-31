package com.example.sw0b_001.Models.Platforms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface StoredPlatformsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(platforms: ArrayList<StoredPlatformsEntity>)
}