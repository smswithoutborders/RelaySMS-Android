package com.example.sw0b_001.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sw0b_001.data.models.StoredPlatformsEntity

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

    @Query("SELECT * FROM StoredPlatformsEntity WHERE account = :account")
    fun fetchAccount(account: String) : StoredPlatformsEntity?

    @Update
    fun update(storedPlatformsEntity: StoredPlatformsEntity)

    @Query("DELETE FROM StoredPlatformsEntity")
    fun deleteAll()

    @Query("DELETE FROM StoredPlatformsEntity WHERE id = :id")
    fun delete(id: String)

    @Query("SELECT id FROM StoredPlatformsEntity")
    fun getAllAccountIds(): List<String>

    @Transaction
    fun insert(platforms: ArrayList<StoredPlatformsEntity>) {
        deleteAll()
        insertAll(platforms)
    }

}