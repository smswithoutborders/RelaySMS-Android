package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Entity
@Serializable
data class Keys(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keystoreAlias: String,
    var privateKey: ByteArray,
    var publicKey: ByteArray,
    var h: ByteArray? = null,
    var ck: ByteArray? = null,
    var nonce: ByteArray? = null,
    var llt: ByteArray? = null,
    var authenticationPublicKeyId: Int? = null,
) : AutoCloseable {
    @Ignore
    @Transient
    private var isClosed = false

    override fun close() {
        privateKey.fill(0)
        publicKey.fill(0)
        nonce?.fill(0)
        h?.fill(0)
        ck?.fill(0)
        authenticationPublicKeyId = null
        isClosed = true
    }
}