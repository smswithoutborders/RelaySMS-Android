package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity
@Serializable
data class SecurityKeys(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var keystoreAlias: String,
    var privateKey: ByteArray,
    var nonce: ByteArray?,
    var sharedSecret: ByteArray? = null,
)