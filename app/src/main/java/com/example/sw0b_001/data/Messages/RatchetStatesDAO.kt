package com.example.sw0b_001.data.Messages

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RatchetStatesDAO {

    @Insert
    fun insert(ratchetStates: RatchetStates)

    @Query("SELECT * FROM RatchetStates")
    fun fetch(): List<RatchetStates>

    @Query("DELETE FROM RatchetStates")
    fun deleteAll()

    @Transaction
    fun update(ratchetStates: RatchetStates) {
        deleteAll()
        insert(ratchetStates)
    }
}