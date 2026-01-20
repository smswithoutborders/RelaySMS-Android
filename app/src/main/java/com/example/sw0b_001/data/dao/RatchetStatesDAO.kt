package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.sw0b_001.data.models.RatchetStates

@Dao
interface RatchetStatesDAO {
    @Query("SELECT * FROM RatchetStates")
    fun fetch(): List<RatchetStates>

    @Query("DELETE FROM RatchetStates")
    fun deleteAll()

    @Transaction
    fun insert(ratchetStates: RatchetStates) {
        deleteAll()
        insert(ratchetStates)
    }
}