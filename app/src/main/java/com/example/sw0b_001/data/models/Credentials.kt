package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity
@Serializable
data class Credentials (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var keystoreAlias: String,
    var llt: ByteArray? = null,
    var deviceID: ByteArray? = null,
    var identityPublicKey: ByteArray? = null,
    var identityPrivateKey: ByteArray? = null,
)