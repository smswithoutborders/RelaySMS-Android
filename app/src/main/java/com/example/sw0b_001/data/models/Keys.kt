package com.example.sw0b_001.data.models

import android.content.Context
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.example.sw0b_001.data.Datastore
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Entity
@Serializable
data class Keys(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyId: UByte,
    val privateKey: ByteArray,
    val publicKey: ByteArray,
    var tokenId: Int? = null,
    var tokenHash: ByteArray? = null,
) : AutoCloseable {
    @Ignore
    @Transient
    private var isClosed = false

    override fun close() {
        privateKey.fill(0)
        publicKey.fill(0)
        tokenHash?.fill(0)
        isClosed = true
    }

    companion object {
        fun getKey(context: Context, tokenHash: ByteArray, keyId: UByte) : Keys {
            val db = Datastore.getDatastore(context)?.keysDao()
                ?: throw Exception("Failed to get database")
            return db.fetch(tokenHash, keyId) ?: throw Exception("No key found")
        }

        fun generate(context: Context, quantity: UByte): List<Keys>{
            val keys = mutableListOf<Keys>()
            val protocols = Protocols(context)

            for(i in 0..quantity.toInt()) {
                try {
                    protocols.generateDH().use { kp ->
                        keys.add(Keys(
                            keyId = i.toUByte(),
                            privateKey = kp.privateKey?.copyOf()
                                ?: throw Exception("No Private key found"),
                            publicKey = kp.publicKey.copyOf()
                        ))
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
            return keys
        }

        fun save(context: Context, tokenHash: ByteArray, keys: List<Keys>, tokenId: Int) {
            val db = Datastore.getDatastore(context)?.keysDao()
                ?: throw Exception("Failed to get database")

            try {
                db.insert(keys, tokenId, tokenHash)
            } catch(e: Exception) {
                throw e;
            } finally {
                keys.forEach { it.close() }
            }
        }

    }

}