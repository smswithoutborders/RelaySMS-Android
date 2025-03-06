package com.example.sw0b_001.Models.Messages

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class EncryptedContent {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var platformName: String? = null

    var type: String? = null

    var fromAccount: String? = null

    var date: Long = 0

    @ColumnInfo(name = "gateway_client_MSISDN")
    var gatewayClientMSISDN: String? = null

    var encryptedContent: String? = null
}
