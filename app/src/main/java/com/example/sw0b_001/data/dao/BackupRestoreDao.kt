package com.example.sw0b_001.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sw0b_001.data.models.BackupRestoreEnt
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupRestoreDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertPrivate(backupRestoreEnt: BackupRestoreEnt)

    @Query("DELETE FROM BackupRestoreEnt")
    fun deleteAll()

    @Transaction
    fun insert(backupRestoreEnt: BackupRestoreEnt) {
        deleteAll()
        insertPrivate(backupRestoreEnt)
    }

    @Query("SELECT * FROM BackupRestoreEnt ORDER BY date DESC LIMIT 1")
    suspend fun fetch() : BackupRestoreEnt?

    @Query("SELECT * FROM BackupRestoreEnt ORDER BY date DESC LIMIT 1")
    fun fetchFlow() : Flow<BackupRestoreEnt?>
}