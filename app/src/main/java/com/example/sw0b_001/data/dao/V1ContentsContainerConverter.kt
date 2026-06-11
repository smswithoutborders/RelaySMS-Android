package com.example.sw0b_001.data.dao

import androidx.room.TypeConverter
import uniffi.relaysms_spec_payload.V1ContentsContainer

class V1ContentsContainerConverter {
    @TypeConverter
    fun fromType(content: ByteArray?): V1ContentsContainer? {
        return content?.let {
            V1ContentsContainer.deserializeFromStorage(content)
        }
    }

    @TypeConverter
    fun contentContainerToByteArray(contentContainer: V1ContentsContainer?): ByteArray? {
        return contentContainer?.let { contentContainer.serializeForStorage() }
    }
}