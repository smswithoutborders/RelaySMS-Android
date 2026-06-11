package com.example.sw0b_001.data.models

import android.content.Context
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.sw0b_001.data.Datastore
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Entity
@Serializable
data class Keys(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyId: Int,
    val privateKey: ByteArray?,
    val publicKey: ByteArray,
    var tokenId: ByteArray?,
    var tokenHash: ByteArray? = null,
    var isOwn: Boolean = true
) : AutoCloseable {
    @Ignore
    @Transient
    private var isClosed = false

    override fun close() {
        privateKey?.fill(0)
        publicKey.fill(0)
        tokenHash?.fill(0)
        tokenId?.fill(0)
        isClosed = true
    }

    companion object {
        fun getOwnKey(context: Context, tokenId: ByteArray, keyId: Int) : Keys {
            val db = Datastore.getDatastore(context)?.keysDao()
                ?: throw Exception("Failed to get database")
            return db.fetch(tokenId, keyId) ?: throw Exception("No key found")
        }
//        suspend fun getOwnKey(context: Context, tokenHash: ByteArray, keyId: UByte) : Keys {
//            val db = Datastore.getDatastore(context)?.keysDao()
//                ?: throw Exception("Failed to get database")
//            return db.fetch(tokenHash, keyId) ?: throw Exception("No key found")
//        }

    }

}