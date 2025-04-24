package com.example.sw0b_001.Models.Platforms

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StoredPlatformsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(platforms: ArrayList<StoredPlatformsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(platform: StoredPlatformsEntity)

    @Query("SELECT * FROM StoredPlatformsEntity")
    fun fetchAll() : LiveData<List<StoredPlatformsEntity>>

    @Query("SELECT * FROM StoredPlatformsEntity")
    fun fetchAllList() : List<StoredPlatformsEntity>

    @Query("SELECT * FROM StoredPlatformsEntity WHERE name = :name")
    fun fetchPlatform(name: String) : LiveData<List<StoredPlatformsEntity>>

    @Query("SELECT * FROM StoredPlatformsEntity WHERE id = :id")
    fun fetch(id: String) : StoredPlatformsEntity

    @Query("DELETE FROM StoredPlatformsEntity")
    fun deleteAll()

    @Query("DELETE FROM StoredPlatformsEntity WHERE id = :id")
    fun delete(id: String)
}