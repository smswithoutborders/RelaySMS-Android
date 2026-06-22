package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Entity(
//    indices = [Index(value = ["tokenId", "tokenHash"], unique = true)]
)
@Serializable
data class Keys(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyId: Int,
    val alias: String,
    var privateKey: ByteArray?,
    val publicKey: ByteArray,
    var tokenHash: ByteArray? = null,
) : AutoCloseable {
    @Ignore
    @Transient
    private var isClosed = false

    override fun close() {
        privateKey?.fill(0)
        publicKey.fill(0)
        tokenHash?.fill(0)
        isClosed = true
    }
}