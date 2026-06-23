package com.example.sw0b_001.data

import android.content.Context
import com.example.sw0b_001.data.models.Keys
import com.example.sw0b_001.data.models.Tokens
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import uniffi.relaysms_spec_payload.BackupRestore
import java.io.ByteArrayOutputStream

class BackupRestore(context: Context) {
    private val db = Datastore.getDatastore(context) ?: throw Exception("Failed to open database")
    val keysDb = db.keysDao() ?: throw Exception("Failed to open keys db")
    val tokensDb = db.tokensDao() ?: throw Exception("Failed to open tokens db")

    @Serializable
    private data class BackupData(
        val keys: ByteArray,
        val tokens: ByteArray
    )

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun backup(): BackupRestore {
        val keys = keysDb.fetchAll()
        val tokens = tokensDb.fetchAllList()

        val sKeys = ByteArrayOutputStream()
        sKeys.use { stream ->
            Json.encodeToStream(keys, stream)
        }

        val sTokens = ByteArrayOutputStream()
        sTokens.use { stream ->
            Json.encodeToStream(tokens, stream)
        }

        val backupData = BackupData(
            keys = sKeys.toByteArray(),
            tokens = sTokens.toByteArray(),
        )

        val sBackups = ByteArrayOutputStream()
        sBackups.use { stream ->
            Json.encodeToStream(backupData, stream)
        }

        val data = sBackups.toByteArray()
        return BackupRestore.v1BackupEncrypt(data)
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun restore(data: ByteArray, digitList: ByteArray) {
        val backupRestore = BackupRestore.deserialize(data, digitList)
        val sBackups = backupRestore.v1RestoreDecrypt()

        try {
            val backups = Json.decodeFromStream<BackupData>(sBackups.inputStream())
            val sKeys = Json.decodeFromStream<List<Keys>>(backups.keys.inputStream())
            val sTokens = Json.decodeFromStream<List<Tokens>>(backups.tokens.inputStream())
            keysDb.deleteAll()
            tokensDb.deleteAll()

            keysDb.insert(sKeys)
            tokensDb.insert(sTokens)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            sBackups.fill(0)
        }
    }
}