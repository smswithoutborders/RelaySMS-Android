package com.example.sw0b_001.Models.Platforms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StoredTokenDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTokens(tokenEntity: StoredTokenEntity)

    @Update
    suspend fun updateTokens(tokenEntity: StoredTokenEntity)

    @Query("SELECT * FROM stored_tokens WHERE account_id = :accountId LIMIT 1")
    suspend fun getTokensByAccountId(accountId: String): StoredTokenEntity?

    @Query("SELECT * FROM stored_tokens")
    suspend fun getAllTokens(): List<StoredTokenEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tokens: List<StoredTokenEntity>)

    @Update
    suspend fun updateAll(tokens: List<StoredTokenEntity>)

    @Query("DELETE FROM stored_tokens WHERE account_id = :accountId")
    suspend fun delete(accountId: String)

    @Query("DELETE FROM stored_tokens")
    suspend fun deleteAllTokens()



}