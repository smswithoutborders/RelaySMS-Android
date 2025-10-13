package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(indices = [Index(value = ["msisdn"], unique = true)])
data class GatewayClients(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var msisdn: String,
    var operator: String,
    var country: String,
    var alias: String? = null,
    var date: Long = System.currentTimeMillis(),
    var isDefault: Boolean = false,
    var last_published_date: Long? = null,
    var manuallyAdded: Boolean = false,
    var operatorCode: String? = null,
    var reliability: Long? = null,
)