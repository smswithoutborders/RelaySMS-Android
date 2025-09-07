package com.example.sw0b_001.Models.GatewayServers

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["url"], unique = true)])
data class GatewayServer(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var publicKey: String,
    var url: String? = null,
    var protocol: String? = null,
    var port: Int = 80,
    var seedsUrl: String? = null
)
