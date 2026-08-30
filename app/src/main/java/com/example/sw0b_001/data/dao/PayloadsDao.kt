package com.example.sw0b_001.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sw0b_001.data.models.Payloads
import kotlinx.coroutines.flow.Flow
import uniffi.relaysms_spec_payload.V1ContentCategories

@Dao
interface PayloadsDao {
    @Insert
    fun insert(payloads: Payloads): Long

    @Query("SELECT * FROM Payloads ORDER BY date DESC")
    fun all(): PagingSource<Int, Payloads>

    @Query("SELECT * FROM Payloads WHERE catId != :catId ORDER BY date DESC")
    fun all(catId: Int): LiveData<MutableList<Payloads>>

    @Query("SELECT * FROM Payloads WHERE catId = :catId ORDER BY date DESC")
    fun inbox(catId: V1ContentCategories): LiveData<MutableList<Payloads>>

    @Query("DELETE FROM Payloads")
    fun deleteAll()

    @Query("DELETE FROM Payloads WHERE id = :id")
    fun delete(id: Long)

    @Delete
    fun delete(message: Payloads)

    @Delete
    fun deleteMultiple(messages: List<Payloads>)

    @Query("SELECT * FROM Payloads WHERE id=:id")
    fun get(id: Long): Payloads?

    @Query("SELECT * FROM Payloads ORDER BY date DESC")
    fun get(): Flow<List<Payloads>>?

    @Query("SELECT * FROM Payloads ORDER BY date DESC")
    fun fetchAll(): List<Payloads>
}