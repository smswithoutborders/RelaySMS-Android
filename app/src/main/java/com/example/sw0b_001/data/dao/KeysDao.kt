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


    @Query("UPDATE Keys SET alias = :newAlias " +
            "WHERE tokenId = :tokenId AND alias = :oldAlias AND keyId < 16"
    )
    suspend fun updateForAttachments(oldAlias: String, newAlias: String, tokenId: Long)

    @Transaction
    suspend fun insert(keys: List<Keys>, alias: String, updateAlias: String? = null) {
        insert(keys)
        updateAlias?.let {
            updateForAttachments(
                alias,
                updateAlias,
                keys.first().tokenId
            )
        }
    }

    @Query("SELECT * FROM Keys WHERE tokenId = :tokenId AND keyId = :keyId AND alias = :alias")
    fun privateFetch(tokenId: Long, keyId: Int, alias: String): Keys?

    @Query(
        "SELECT * FROM Keys WHERE tokenId = :tokenId AND alias = :alias " +
            "ORDER BY RANDOM() LIMIT 1")
    fun privateFetchRandom(tokenId: Long, alias: String): Keys?

    @Transaction
    fun fetchEphemeral(tokenId: Long, alias: String, keyId: Int? = null): Keys? {
        val key = if(keyId == null) privateFetchRandom(tokenId, alias)
        else privateFetch(tokenId, keyId, alias)
        key?.let { remove(key) } // removes the key as it's used
        return key
    }

    @Query("DELETE FROM Keys WHERE tokenId = :tokenHash")
    suspend fun clear(tokenHash: ByteArray)

    @Delete
    fun remove(key: Keys)

    @Query("SELECT * FROM Keys")
    suspend fun fetchAll(): List<Keys>

    @Query("DELETE FROM Keys")
    suspend fun deleteAll()
}