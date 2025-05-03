package com.example.sw0b_001.Models.Platforms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StoredTokenDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTokens(tokenEntity: StoredTokenEntity)

    @Update
    fun updateTokens(tokenEntity: StoredTokenEntity)

    @Query("SELECT * FROM stored_tokens WHERE account_id = :accountId LIMIT 1")
    fun getTokensByAccountId(accountId: String): StoredTokenEntity?

    @Query("SELECT * FROM stored_tokens")
    fun getAllTokens(): List<StoredTokenEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tokens: List<StoredTokenEntity>)

    @Update
    fun updateAll(tokens: List<StoredTokenEntity>)

    @Query("DELETE FROM stored_tokens WHERE account_id = :accountId")
    fun delete(accountId: String)

    @Query("DELETE FROM stored_tokens")
    fun deleteAllTokens()



}