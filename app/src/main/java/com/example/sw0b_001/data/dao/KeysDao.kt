package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sw0b_001.data.models.Keys


@Dao
interface KeysDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun _insert(key: List<Keys>)

    @Transaction
    fun insert(keys: List<Keys>, tokenId: Int, tokenHash: ByteArray) {
        clear(tokenId)
        keys.forEach {
            it.tokenId = tokenId
            it.tokenHash = tokenHash
        }
        _insert(keys)
    }

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId")
    fun fetchTokenId(tokenId: Int): List<Keys>?

    @Query("SELECT * FROM Keys WHERE tokenHash = :tokenHash AND keyId = :keyId")
    fun fetch(tokenHash: ByteArray, keyId: UByte): Keys?

    @Query("DELETE FROM Keys WHERE tokenId = :tokenId")
    fun clear(tokenId: Int)
}