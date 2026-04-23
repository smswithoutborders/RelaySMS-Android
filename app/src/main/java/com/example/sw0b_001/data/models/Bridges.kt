package com.example.sw0b_001.data.models

import android.content.Context
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.Cryptography
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.RatchetPayload
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.RatchetsHE
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.extensions.context.getStaticKeys
import org.bouncycastle.crypto.params.X25519PublicKeyParameters

class Bridges(val context: Context) {
    private val protocols = Protocols(context)
    private val staticKeyAlias = "Bridges_Static_KeystoreAlias"
    private val ephemeralKeyAlias = "Ephemeral_Static_KeystoreAlias"
    private val ratchetKeystoreAlias = "Bridge_Ratchet_KeystoreAlias"

    fun forSendingWithoutForwardSecrecy(plaintext: ByteArray): RatchetPayload? {
        val authenticationPublicKeyId = (0..255).random()

        val authenticationPublicKey = context.getStaticKeys(authenticationPublicKeyId)
            ?: throw Exception("Could not find static keys for id")

        val info = "RelaySMS C2S DR v1".encodeToByteArray()
        val headerInfo = "RelaySMS C2S DRHE v1".encodeToByteArray()

        val ephemeralKeyPair = protocols.generateDH()
        val staticKeyPair = protocols.generateDH()

        var ratchetPayload: RatchetPayload? = null
        try {
            ephemeralKeyPair.use { ephemeralKeyPair ->
                staticKeyPair.use { staticKeyPair ->
                    val keys = Cryptography.generateKeysIK(
                        context = context,
                        ephemeralKeyPair = ephemeralKeyPair,
                        authenticationPublicKey = authenticationPublicKey,
                        staticKeyPair = staticKeyPair,
                        info = info,
                        headerInfo = headerInfo
                    )

                    try {
                        keys.use { keys ->
                            val ratchet = RatchetsHE(context)
                            val state = States()
                            state.use { persistentState ->
                                ratchet.ratchetInitAlice(
                                    state = persistentState,
                                    sk = keys.rk,
                                    bobDhPublicKey = authenticationPublicKey,
                                    sharedHka = keys.hk,
                                    sharedNHka = keys.nhk
                                )

                                ratchetPayload = ratchet.ratchetEncrypt(
                                    state = persistentState,
                                    plaintext = plaintext,
                                    ad = keys.h!!
                                )

                                saveParametersForForwardSecrecy(
                                    staticKeyPair,
                                    ephemeralKeyPair,
                                    keys.h!!,
                                    keys.ck!!,
                                    authenticationPublicKeyId
                                )
                            }
                        }
                    } finally {
                        keys.close()
                    }
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            authenticationPublicKey.fill(0)
            info.fill(0)
            headerInfo.fill(0)

            ephemeralKeyPair.close()
            staticKeyPair.close()
        }

        return ratchetPayload
    }

    private fun saveParametersForForwardSecrecy(
        staticKeyPair: Protocols.CloseableCurve15519KeyPair,
        ephemeralKeyPair: Protocols.CloseableCurve15519KeyPair,
        h: ByteArray,
        ck: ByteArray,
        authenticationPublicKeyId: Int,
    ) {
        val staticKeys = Keys(
            keystoreAlias = staticKeyAlias,
            authenticationPublicKeyId = authenticationPublicKeyId,
            privateKey = staticKeyPair.privateKey!!,
            publicKey = staticKeyPair.publicKey
        )

        val db = Datastore.getDatastore(context)?.keysDao()
            ?: throw Exception("Failed to open database")

        try {
            db.insert(staticKeys)
        } catch(e: Exception) {
            throw e
        }

        val ephemeralKeys = Keys(
            keystoreAlias = ephemeralKeyAlias,
            privateKey = ephemeralKeyPair.privateKey!!,
            publicKey = ephemeralKeyPair.publicKey,
            h = h,
            ck = ck,
        )

        try {
            db.insert(ephemeralKeys)
        } catch(e: Exception) {
            throw e
        }
    }

    fun forSendingWithForwardSecrecy(
        context: Context,
        plaintext: ByteArray,
        staticPublicKey: X25519PublicKeyParameters,
        staticPublicKeyId: Int,
    ): RatchetPayload? {
        var ratchetPayload: RatchetPayload? = null

        val authenticationPublicKey = context.getStaticKeys(staticPublicKeyId)?.run {
            X25519PublicKeyParameters(this, 0)
        } ?: throw Exception("Could not find static keys for id: $staticPublicKeyId")

        val ad = "RelaySMS AD v1".encodeToByteArray() +
                staticPublicKey.encoded +
                authenticationPublicKey.encoded

        val ratchet = RatchetsHE(context)
        val db = Datastore.getDatastore(context)?.ratchetStatesDAO()
            ?: throw Exception("Failed to open database")
        val ratchetState = db.fetch(ratchetKeystoreAlias)
            ?: throw Exception("No state found for Ratchets")

        try {
            ratchetState.use { rs ->
                val deserializedState = States.deserialize(rs.value)
                deserializedState.use { state ->
                    ratchetPayload = ratchet.ratchetEncrypt(
                        state = state,
                        plaintext = plaintext,
                        ad = ad
                    )
                    val persistentState = RatchetStates(
                        value = state.serialize(),
                        keystoreAlias = ratchetKeystoreAlias
                    )
                    persistentState.use { rs ->
                        rs.save(context)
                    }
                }
            }
        } catch (e: Exception) {
            throw e
        } finally {
            authenticationPublicKey.encoded.fill(0)
            ad.fill(0)
        }
        return ratchetPayload
    }

    fun initRatchet() {
        val info = "RelaySMS C2S DR v1".encodeToByteArray()
        val headerInfo = "RelaySMS C2S DRHE v1".encodeToByteArray()

        val db = Datastore.getDatastore(context)?.keysDao()
            ?: throw Exception("Failed to open database")

        val authenticationPublicKeyId = db.fetchAuthenticationId(staticKeyAlias)
            ?: throw Exception("No authentication Id found")

        val authenticationPublicKey = context.getStaticKeys(authenticationPublicKeyId)
            ?: throw Exception("Could not find static keys for id")

        val ephemeralKeys = db.fetch(ephemeralKeyAlias)
            ?: throw Exception("No Ephemeral keys found")

        try {
            ephemeralKeys.use { ephemeralKeys ->
                val ephemeralKeyPair = Protocols.CloseableCurve15519KeyPair(
                    publicKey = ephemeralKeys.publicKey,
                    privateKey = ephemeralKeys.privateKey
                )
                ephemeralKeyPair.use {
                    val keys = Cryptography.generateKeysIKForwardSecrecy(
                        context = context,
                        h = ephemeralKeys.h!!,
                        ck = ephemeralKeys.ck!!,
                        ephemeralKeyPair = ephemeralKeyPair,
                        ephemeralResponderPublicKey = ephemeralKeys.publicKey,
                        authenticationPublicKey = authenticationPublicKey,
                        info = info,
                        headerInfo = headerInfo
                    )
                    keys.use { keys ->
                        RatchetStates.initialize(
                            context = context,
                            keystoreAlias = ratchetKeystoreAlias,
                            authenticationPublicKey = authenticationPublicKey,
                            rk = keys.rk,
                            hk = keys.hk,
                            nhk = keys.nhk,
                        )
                    }
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            info.fill(0)
            headerInfo.fill(0)
            authenticationPublicKey.fill(0)
            ephemeralKeys.close()
        }
    }
}