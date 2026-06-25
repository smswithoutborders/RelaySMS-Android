package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity
data class BackupRestoreEnt(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val uri: String,
    val recovery_key: ByteArray? = null,
    var date: Long = System.currentTimeMillis(),
) : AutoCloseable {
    @Ignore
    private var isClosed = false

    override fun close() {
        if(!isClosed) {
            recovery_key?.fill(0)
            isClosed = true
        }
    }
}