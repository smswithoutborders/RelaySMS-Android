package com.example.sw0b_001.data

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.AutoCloseable

@Entity
data class OAuth(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val platformName: String,
    val codeVerifier: ByteArray,
    val requestId: ByteArray
) : AutoCloseable{
    fun save(context: Context) {
        val db = Datastore.getDatastore(context)?.oAuthDao()
            ?: throw Exception("Cannot find database")
        try {
            db.insert(this)
        } catch (e: Exception) {
            throw e
        }
    }

    fun clear(context: Context) {
        val db = Datastore.getDatastore(context)?.oAuthDao()
            ?: throw Exception("Cannot find database")
        try {
            db.remove(this)
        } catch (e: Exception) {
            throw e
        }
    }


    @Transient
    private var isClosed = false
    override fun close() {
        if(!isClosed) {
            codeVerifier.fill(0)
            requestId.fill(0)
            isClosed = true
        }
    }
}
