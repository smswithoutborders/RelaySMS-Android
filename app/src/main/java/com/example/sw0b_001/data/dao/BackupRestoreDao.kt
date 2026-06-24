package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sw0b_001.data.models.BackupRestoreEnt

@Dao
interface BackupRestoreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(backupRestoreEnt: BackupRestoreEnt)

    @Query("SELECT * FROM BackupRestoreEnt ORDER BY date DESC LIMIT 1")
    suspend fun fetch() : BackupRestoreEnt?
}