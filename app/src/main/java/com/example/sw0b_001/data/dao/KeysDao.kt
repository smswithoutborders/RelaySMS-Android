package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sw0b_001.data.models.Keys


@Dao
interface KeysDao {

    @Query("DELETE FROM Keys WHERE keystoreAlias = :keystoreAlias")
    fun remove(keystoreAlias: String)

    @Update
    fun update(key: Keys)

    @Insert
    fun _insert(key: Keys)

    @Transaction
    fun insert(key: Keys) {
        clear(key.keystoreAlias)
        _insert(key)
    }

    @Query("SELECT * FROM Keys WHERE keystoreAlias = :keystoreAlias")
    fun fetch(keystoreAlias: String): Keys?

    @Query("SELECT authenticationPublicKeyId FROM Keys WHERE keystoreAlias = :keystoreAlias")
    fun fetchAuthenticationId(keystoreAlias: String): Int?

    @Query("SELECT llt FROM Keys WHERE keystoreAlias = :keystoreAlias")
    fun fetchLlt(keystoreAlias: String): ByteArray?

    @Query("SELECT publicKey FROM Keys WHERE keystoreAlias = :keystoreAlias")
    fun fetchPublicKey(keystoreAlias: String): ByteArray?

    @Query("DELETE FROM Keys where keystoreAlias = :keystoreAlias")
    fun clear(keystoreAlias: String)
}