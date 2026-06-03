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
    suspend fun insert(keys: List<Keys>, tokenId: Int, tokenHash: ByteArray) {
        clear(tokenId)
        keys.forEach {
            it.tokenId = tokenId
            it.tokenHash = tokenHash
        }
        _insert(keys)
    }

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId")
    fun fetchTokenId(tokenId: Int): List<Keys>?

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId AND keyId = :keyId")
    suspend fun _fetch(tokenId: Int, keyId: UByte): Keys?

    @Transaction
    suspend fun fetch(tokenId: Int, keyId: UByte): Keys? {
        val key = _fetch(tokenId, keyId)
        key?.let { remove(key) }
        return key
    }

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId AND keyId = :keyId AND NOT isOwn")
    suspend fun fetchOthers(tokenId: Int, keyId: UByte): Keys?

    @Query("DELETE FROM Keys WHERE tokenId = :tokenId")
    suspend fun clear(tokenId: Int)

    @Delete
    suspend fun remove(key: Keys)
}