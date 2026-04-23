package com.example.sw0b_001.data

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OAuth(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val platformName: String,
    val codeVerifier: String
) {
    fun save(context: Context) {
        val db = Datastore.getDatastore(context)?.oAuthDao()
            ?: throw Exception("Cannot find database")
        try {
            db.insert(this)
        } catch (e: Exception) {
            throw e
        }
    }
}
