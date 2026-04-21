package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity
@Serializable
data class Keys(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keystoreAlias: String,
    var privateKey: ByteArray,
    var publicKey: ByteArray,
    var responderEphemeralPublicKey: ByteArray? = null,
    var h: ByteArray? = null,
    var ck: ByteArray? = null,
    var authenticationPublicKeyId: Int? = null,
) : AutoCloseable {
    private var isClosed = false
    fun use(block: (Keys) -> Unit) {
        if (isClosed) throw IllegalStateException("Identity key already closed")
        block(this)
    }

    override fun close() {
        privateKey.fill(0)
        publicKey.fill(0)
        responderEphemeralPublicKey?.fill(0)
        h?.fill(0)
        ck?.fill(0)
        authenticationPublicKeyId = null
        isClosed = true
    }
}