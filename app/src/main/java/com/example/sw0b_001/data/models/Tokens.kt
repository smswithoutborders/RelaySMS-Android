package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import uniffi.relaysms_spec_payload.V1ContentCategories

@Entity(indices = [Index(value = ["tokenId"], unique = true)])
data class Tokens(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tokenId: Int,
    val catId: V1ContentCategories,
    val account: String,
    val platformName: String,
)