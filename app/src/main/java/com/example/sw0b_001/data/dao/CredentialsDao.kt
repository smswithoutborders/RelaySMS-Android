package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sw0b_001.data.models.Credentials


@Dao
interface CredentialsDao {

    @Query("DELETE FROM Credentials")
    fun clear()

    @Update
    fun update(credentials: Credentials)

    @Insert
    fun insert(credentials: Credentials)

    @Query("SELECT * FROM Credentials WHERE keystoreAlias = :keystoreAlias")
    fun fetch(keystoreAlias: String): Credentials?
}