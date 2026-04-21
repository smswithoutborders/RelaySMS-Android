package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Messages {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var platformLetter: Byte? = null
    var type: Byte? = null
    var fromAccount: ByteArray? = null
    var date: Long = 0
    var body: ByteArray? = null
}