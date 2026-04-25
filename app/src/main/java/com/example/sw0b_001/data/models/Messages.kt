package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Messages(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var accountId: Int,
    var type: Byte,
    var date: Long,
    var to: ByteArray,
    var platformId: Int? = null,
    var cc: ByteArray? = null,
    var bcc: ByteArray? = null,
    var subject: ByteArray? = null,
    var body: ByteArray? = null,
    var image: ByteArray? = null
)