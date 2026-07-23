package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import uniffi.relaysms_spec_payload.V1ContentCategories
import java.security.MessageDigest

@Entity(indices = [Index(value = ["tokenHash"], unique = true)])
@Serializable
data class Tokens(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tokenId: Int,
    val tokenHash: ByteArray,
    val catId: V1ContentCategories,
    val account: String,
    val platformName: String,
    val date: Long = System.currentTimeMillis(),
): AutoCloseable {

    @Ignore
    private var isClosed = false
    override fun close() {
        if(!isClosed) {
            this.tokenHash.fill(0)
            isClosed = true
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Tokens

        // Non-sensitive fields first (fast path for obvious mismatches)
        if (id != other.id) return false
        if (tokenId != other.tokenId) return false
        if (catId != other.catId) return false
        if (account != other.account) return false
        if (platformName != other.platformName) return false

        // Constant-time comparison for the sensitive byte array
        if (!MessageDigest.isEqual(tokenHash, other.tokenHash)) return false

        return true
    }

    override fun hashCode(): Int {
        // tokenHash drives the hash — derive a stable Int from a cryptographic digest
        // so we don't leak raw bytes into HashMap bucket indices
        val digest = MessageDigest.getInstance("SHA-256").digest(tokenHash)
        return digest.take(4)
            .foldIndexed(0) { i, acc, b -> acc or ((b.toInt() and 0xFF) shl (i * 8)) }
    }
}
