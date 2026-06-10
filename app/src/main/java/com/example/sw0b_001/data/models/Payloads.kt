package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer

@Entity
data class Payloads(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val catId: V1ContentCategories,
    val payload: ByteArray,
    val isAttachment: Boolean,
    val date: Long = System.currentTimeMillis(),
): AutoCloseable {
    override fun close() {
        payload.fill(0)
    }
    @Ignore
    var contents: V1ContentsContainer? = null
}