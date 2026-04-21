package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.sw0b_001.data.models.Keys


@Dao
interface KeysDao {

    @Query("DELETE FROM Keys WHERE keystoreAlias = :keystoreAlias")
    fun remove(keystoreAlias: String)

    @Insert
    fun _insert(key: Keys)

    @Transaction
    fun insert(key: Keys) {
        clear()
        _insert(key)
    }

    @Query("SELECT * FROM Keys WHERE keystoreAlias = :keystoreAlias")
    fun fetch(keystoreAlias: String): Keys

    @Query("SELECT authenticationPublicKeyId FROM Keys WHERE keystoreAlias = :keystoreAlias")
    fun fetchAuthenticationId(keystoreAlias: String): Int?

    @Query("DELETE FROM Keys")
    fun clear()
}