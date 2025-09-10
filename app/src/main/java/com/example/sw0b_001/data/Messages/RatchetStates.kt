package com.example.sw0b_001.data.Messages

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RatchetStates (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @ColumnInfo(name="value", typeAffinity = ColumnInfo.BLOB)
    val value: ByteArray
)