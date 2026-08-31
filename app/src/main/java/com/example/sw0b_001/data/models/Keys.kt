package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.security.MessageDigest


@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Tokens::class,
            parentColumns = ["id"],
            childColumns = ["tokenId"],
            onDelete = ForeignKey.CASCADE // This triggers the automatic deletion
        )
    ],
    indices = [Index(value = ["tokenId"])]
)
@Serializable
data class Keys(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyId: Int,
    val alias: String,
    var privateKey: ByteArray?,
    val publicKey: ByteArray,
    val tokenId: Long,
    val date: Long = System.currentTimeMillis(),
) : AutoCloseable {
    @Ignore
    @Transient
    private var isClosed = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Keys

        if (id != other.id) return false
        if (keyId != other.keyId) return false
        if (alias != other.alias) return false

        // All ByteArray fields use constant-time comparison
        if (!MessageDigest.isEqual(publicKey, other.publicKey)) return false

        // Null-safe constant-time comparison for nullable fields
        val thisPrivate = privateKey
        val otherPrivate = other.privateKey
        if (thisPrivate == null != (otherPrivate == null)) return false
        if (thisPrivate != null && otherPrivate != null &&
            !MessageDigest.isEqual(thisPrivate, otherPrivate)) return false

        return true
    }

    override fun hashCode(): Int {
        val digest = MessageDigest.getInstance("SHA-256").digest(publicKey)
        return digest.take(4)
            .foldIndexed(0) { i, acc, b -> acc or ((b.toInt() and 0xFF) shl (i * 8)) }
    }

    override fun close() {
        privateKey?.fill(0)
        publicKey.fill(0)
        isClosed = true
    }
}