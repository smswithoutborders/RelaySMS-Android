package com.example.sw0b_001.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sw0b_001.data.models.Accounts

@Dao
interface StoredPlatformsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(platforms: List<Accounts>)

    @Query("SELECT * FROM Accounts")
    fun fetchAll() : LiveData<List<Accounts>>

    @Query("SELECT * FROM Accounts")
    fun fetchAllList() : List<Accounts>

    @Query("SELECT * FROM Accounts where name = :platformName")
    fun fetchAllList(platformName: String) : List<Accounts>

    @Query("SELECT * FROM Accounts WHERE name = :name")
    fun fetchPlatform(name: String) : LiveData<List<Accounts>>

    @Query("SELECT * FROM Accounts WHERE id = :id")
    fun fetch(id: String) : Accounts

    @Query("SELECT * FROM Accounts WHERE account = :account")
    fun fetchAccount(account: String) : Accounts?

    @Update
    fun update(accounts: Accounts)

    @Query("DELETE FROM Accounts")
    fun deleteAll()

    @Query("DELETE FROM Accounts WHERE id = :id")
    fun delete(id: String)

    @Query("SELECT id FROM Accounts")
    fun getAllAccountIds(): List<String>

    @Transaction
    fun insert(platforms: List<Accounts>) {
        deleteAll()
        insertAll(platforms)
    }

}