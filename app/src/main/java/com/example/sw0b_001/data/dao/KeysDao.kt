package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sw0b_001.data.models.Keys


@Dao
interface KeysDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun _insert(key: List<Keys>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(keys: List<Keys>)

    @Query("DELETE FROM Keys WHERE alias = :alias")
    suspend fun removeAlias(alias: String)

    @Transaction
    suspend fun insert(keys: List<Keys>, alias: String) {
        removeAlias(alias)
        insert(keys)
    }

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId")
    fun fetchTokenId(tokenId: ByteArray): List<Keys>?

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId AND keyId = :keyId AND alias = :alias")
    fun _fetch(tokenId: ByteArray, keyId: Int, alias: String): Keys?

    @Transaction
    fun fetch(tokenId: ByteArray, keyId: Int, alias: String): Keys? {
        val key = _fetch(tokenId, keyId, alias)
        key?.let { remove(key) } // removes the key as it's used
        return key
    }

    @Query("DELETE FROM Keys WHERE tokenId = :tokenId")
    suspend fun clear(tokenId: ByteArray)

    @Delete
    fun remove(key: Keys)
}