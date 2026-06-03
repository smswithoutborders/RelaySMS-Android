package com.example.sw0b_001.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sw0b_001.data.models.Tokens

@Dao
interface TokensDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(platforms: List<Tokens>)

    @Query("SELECT * FROM Tokens")
    fun fetchAll() : LiveData<List<Tokens>>

    @Query("SELECT * FROM Tokens")
    fun fetchAllList() : List<Tokens>

    @Query("SELECT * FROM Tokens where platformName = :platformName")
    fun fetchAllList(platformName: String) : List<Tokens>

    @Query("SELECT * FROM Tokens WHERE platformName = :name")
    fun fetchPlatform(name: String) : LiveData<List<Tokens>>

    @Query("SELECT * FROM Tokens WHERE id = :id")
    fun fetch(id: Int) : Tokens

    @Query("SELECT * FROM Tokens WHERE account = :account")
    fun fetchAccount(account: String) : Tokens?

    @Update
    fun update(tokens: Tokens)

    @Query("DELETE FROM Tokens")
    fun deleteAll()

    @Query("DELETE FROM Tokens WHERE id = :id")
    fun delete(id: Int)

    @Transaction
    fun insert(platforms: List<Tokens>) {
        deleteAll()
        insertAll(platforms)
    }

}