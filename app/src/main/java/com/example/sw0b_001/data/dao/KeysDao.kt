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

    @Transaction
    suspend fun insert(keys: List<Keys>, tokenId: ByteArray, tokenHash: ByteArray) {
        clear(tokenId)
        keys.forEach {
            it.tokenId = tokenId
            it.tokenHash = tokenHash
        }
        _insert(keys)
    }

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId")
    fun fetchTokenId(tokenId: ByteArray): List<Keys>?

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId AND keyId = :keyId")
    fun _fetch(tokenId: ByteArray, keyId: Int): Keys?

    @Transaction
    fun fetch(tokenId: ByteArray, keyId: Int): Keys? {
        val key = _fetch(tokenId, keyId)
        key?.let { remove(key) }
        return key
    }

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId AND keyId = :keyId AND NOT isOwn")
    fun fetchOthers(tokenId: ByteArray, keyId: Int): Keys?

    @Query("DELETE FROM Keys WHERE tokenId = :tokenId")
    suspend fun clear(tokenId: ByteArray)

    @Delete
    fun remove(key: Keys)
}