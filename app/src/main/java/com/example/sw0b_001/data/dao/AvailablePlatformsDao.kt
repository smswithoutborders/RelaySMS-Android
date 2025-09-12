package com.example.sw0b_001.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sw0b_001.data.models.AvailablePlatforms

@Dao
interface AvailablePlatformsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(platforms: ArrayList<AvailablePlatforms>)

    @Query("SELECT * FROM AvailablePlatforms")
    fun fetchAll() : LiveData<List<AvailablePlatforms>>

    @Query("SELECT * FROM AvailablePlatforms")
    fun fetchAllList() : List<AvailablePlatforms>

    @Query("SELECT * FROM AvailablePlatforms WHERE name = :name")
    fun fetch(name: String) : AvailablePlatforms?

    @Query("DELETE FROM AvailablePlatforms WHERE name = :name")
    fun delete(name: String)

    @Query("DELETE FROM AvailablePlatforms")
    fun clear()
}