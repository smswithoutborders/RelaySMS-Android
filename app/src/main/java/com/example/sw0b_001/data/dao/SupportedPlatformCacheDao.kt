package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportedPlatformCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(supportedPlatform: SupportedPlatforms)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun _insertBulk(supportedPlatform: List<SupportedPlatforms>)

    @Transaction
    fun insert(supportedPlatform: List<SupportedPlatforms>) {
//        clear()
        _insertBulk(supportedPlatform)
    }

    @Query("select * from SupportedPlatforms where name = :name")
    fun fetch(name: String): SupportedPlatforms?

    @Query("select * from SupportedPlatforms")
    fun fetch(): Flow<List<SupportedPlatforms>>

    @Query("select * from SupportedPlatforms")
    fun fetchDebug(): List<SupportedPlatforms>

    @Query("delete from supportedplatforms")
    fun clear()
}