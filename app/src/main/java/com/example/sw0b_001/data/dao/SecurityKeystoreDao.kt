package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sw0b_001.data.models.SecurityKeys


@Dao
interface SecurityKeystoreDao {

    @Query("DELETE FROM SecurityKeys WHERE keystoreAlias = :keystoreAlias")
    fun remove(keystoreAlias: String)

    @Update
    fun update(securityKeys: SecurityKeys)

    @Insert
    fun insert(securityKeys: SecurityKeys)

    @Query("SELECT * FROM SecurityKeys WHERE keystoreAlias = :keystoreAlias")
    fun fetch(keystoreAlias: String): SecurityKeys
}