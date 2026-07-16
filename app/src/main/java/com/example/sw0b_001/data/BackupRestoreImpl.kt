package com.example.sw0b_001.data

import android.content.Context
import android.util.Log
import com.example.sw0b_001.data.models.Keys
import com.example.sw0b_001.data.models.Tokens
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import uniffi.relaysms_spec_payload.BackupRestore
import java.io.ByteArrayOutputStream

class BackupRestoreImpl(context: Context) {
    private val db = Datastore.getDatastore(context) ?: throw Exception("Failed to open database")
    val keysDb = db.keysDao() ?: throw Exception("Failed to open keys db")
    val tokensDb = db.tokensDao() ?: throw Exception("Failed to open tokens db")
    val backupRestoreDb = db.backupRestoreDao() ?: throw Exception("Failed to open backup/resetore db")

    @Serializable
    private data class BackupData(
        val keys: ByteArray,
        val tokens: ByteArray
    )

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun backup(): BackupRestore {
        val keys = keysDb.fetchAll()
        val tokens = tokensDb.fetchAllList()
        val backupRestore = backupRestoreDb.fetch()

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
        return BackupRestore.v1BackupEncrypt(data, backupRestore?.recovery_key)
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun restore(data: ByteArray, recoveryKey: ByteArray) {
        val backupRestore = BackupRestore.deserialize(data, recoveryKey)
        val sBackups = backupRestore.v1RestoreDecrypt()

        try {
            val backups = Json.decodeFromStream<BackupData>(sBackups.inputStream())
            val sKeys = Json.decodeFromStream<List<Keys>>(backups.keys.inputStream())
            val sTokens = Json.decodeFromStream<List<Tokens>>(backups.tokens.inputStream())
            tokensDb.deleteAll()
            keysDb.deleteAll()

            val orphanedKeys = sKeys.filter { key ->
                key.tokenHash != null && sTokens.none {
                    it.tokenHash.contentEquals(key.tokenHash!!)
                }
            }
            Log.w("Restore", "Orphaned keys: ${orphanedKeys.size}")

            db.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys = OFF")
            tokensDb.insert(sTokens)
            keysDb.insert(sKeys)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            sBackups.fill(0)
            db.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys = ON")
        }
    }
}