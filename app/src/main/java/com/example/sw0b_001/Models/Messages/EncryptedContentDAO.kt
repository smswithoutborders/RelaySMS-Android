package com.example.sw0b_001.Models.Messages

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sw0b_001.Bridges.Bridges
import com.example.sw0b_001.Models.Platforms.Platforms

@Dao
interface EncryptedContentDAO {
    @Insert
    fun insert(encryptedContent: EncryptedContent): Long

    @Query("SELECT * FROM EncryptedContent WHERE type != :type ORDER BY date DESC")
    fun all(type: String): LiveData<MutableList<EncryptedContent>>

    @Query("SELECT * FROM EncryptedContent WHERE type = :type ORDER BY date DESC")
    fun inbox(type: String): LiveData<MutableList<EncryptedContent>>

    @Query("DELETE FROM EncryptedContent")
    fun deleteAll()

    @Query("DELETE FROM EncryptedContent WHERE id = :id")
    fun delete(id: Long)

    @Delete
    fun delete(message: EncryptedContent)

    @Delete
    fun deleteMultiple(messages: List<EncryptedContent>)

    @Query("SELECT * FROM EncryptedContent WHERE id=:encryptedContentId")
    fun get(encryptedContentId: Long): EncryptedContent

    @Query("SELECT * FROM EncryptedContent WHERE encryptedContent LIKE '%' || :filterText || '%'")
    fun getForFilterText(filterText: String?): MutableList<EncryptedContent>?
}
