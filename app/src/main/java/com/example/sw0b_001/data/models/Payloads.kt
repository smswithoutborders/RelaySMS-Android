package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import uniffi.relaysms_spec_payload.V1ContentCategories

@Entity
data class Payloads(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val catId: V1ContentCategories,
    val payload: ByteArray,
    val date: Long = System.currentTimeMillis(),
): AutoCloseable {
    override fun close() {
        payload.fill(0)
    }
}