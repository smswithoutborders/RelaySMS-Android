package com.example.sw0b_001.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sw0b_001.data.models.Messages

@Dao
interface MessagesDao {
    @Insert
    fun insert(messages: Messages): Long

    @Query("SELECT * FROM Messages ORDER BY date DESC")
    fun all(): PagingSource<Int, Messages>

    @Query("SELECT * FROM Messages WHERE type != :type ORDER BY date DESC")
    fun all(type: String): LiveData<MutableList<Messages>>

    @Query("SELECT * FROM Messages WHERE type = :type ORDER BY date DESC")
    fun inbox(type: Byte): LiveData<MutableList<Messages>>

    @Query("DELETE FROM Messages")
    fun deleteAll()

    @Query("DELETE FROM Messages WHERE id = :id")
    fun delete(id: Long)

    @Delete
    fun delete(message: Messages)

    @Delete
    fun deleteMultiple(messages: List<Messages>)

    @Query("SELECT * FROM Messages WHERE id=:id")
    fun get(id: Long): Messages?

    @Query("SELECT * FROM Messages WHERE id=:encryptedContentId")
    fun getLiveData(encryptedContentId: Long): LiveData<Messages>

    @Query("SELECT * FROM Messages WHERE body LIKE '%' || :filterText || '%'")
    fun getForFilterText(filterText: String?): MutableList<Messages>?
}