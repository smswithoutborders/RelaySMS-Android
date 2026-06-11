package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.sw0b_001.data.dao.V1ContentsContainerConverter
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer

@Entity
@TypeConverters(V1ContentsContainerConverter::class)
data class Payloads(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val catId: V1ContentCategories,
    val content: V1ContentsContainer,
    val date: Long = System.currentTimeMillis(),
//    var contents: V1ContentsContainer? = null
): AutoCloseable {
    override fun close() {
//        payload.fill(0)
    }
}