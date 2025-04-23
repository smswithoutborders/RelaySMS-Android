package com.example.sw0b_001.Models.Platforms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StoredTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTokens(tokenEntity: StoredTokenEntity)

    @Query("SELECT * FROM stored_tokens WHERE account_id = :accountId LIMIT 1")
    suspend fun getTokensByAccountId(accountId: String): StoredTokenEntity?

    @Query("SELECT * FROM stored_tokens")
    suspend fun getAllTokens(): List<StoredTokenEntity>


}