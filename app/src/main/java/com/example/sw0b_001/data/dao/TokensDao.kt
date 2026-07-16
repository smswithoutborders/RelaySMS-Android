package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sw0b_001.data.models.Tokens
import kotlinx.coroutines.flow.Flow

@Dao
interface TokensDao {
    @Query("SELECT * FROM Tokens")
    fun fetchAll() : Flow<List<Tokens>>

    @Query("SELECT * FROM Tokens")
    suspend fun fetchAllList() : List<Tokens>

    @Query("SELECT * FROM Tokens where tokenHash = :tokenHash")
    fun fetch(tokenHash: ByteArray) : Tokens?

    @Query("SELECT * FROM Tokens where id = :id")
    fun fetch(id: Int) : Tokens?

    @Query("SELECT * FROM Tokens WHERE platformName = :name")
    fun fetch(name: String) : Flow<List<Tokens>>


    @Insert
    fun insert(tokens: Tokens)

    @Insert
    fun insert(tokens: List<Tokens>)

    @Delete
    fun delete(tokens: Tokens)

    @Query("DELETE FROM Tokens")
    suspend fun deleteAll()
}