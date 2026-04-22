package com.example.sw0b_001.data.models

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.RatchetsHE
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.data.Datastore
import org.bouncycastle.crypto.CipherParameters
import java.lang.AutoCloseable

@Entity
data class RatchetStates (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keystoreAlias: String,
    val value: ByteArray
): AutoCloseable {
    private var isClosed = false
    override fun close() {
        if(isClosed) return
        value.fill(0)
        isClosed = true
    }

    fun save(context: Context) {
        if (isClosed) throw IllegalStateException("Cannot save a closed state")

        try {
            Datastore.getDatastore(context)?.ratchetStatesDAO()?.insert(this)
        } catch (e: Exception) {
            close()
            throw e
        }
    }

    companion object {
        fun initialize(
            context: Context,
            keystoreAlias: String,
            authenticationPublicKey: CipherParameters,
            rk: ByteArray,
            hk: ByteArray,
            nhk: ByteArray,
        ) {
            val ratchet = RatchetsHE(context)
            val state = States()
            state.use { closeableState ->
                ratchet.ratchetInitAlice(
                    state = closeableState,
                    sk = rk,
                    bobDhPublicKey = authenticationPublicKey,
                    sharedHka = hk,
                    sharedNHka = nhk
                )
                val ratchetStates = RatchetStates(
                    value = closeableState.serialize(),
                    keystoreAlias = keystoreAlias
                )
                ratchetStates.use { rs ->
                    rs.save(context)
                }
            }
        }
    }
}