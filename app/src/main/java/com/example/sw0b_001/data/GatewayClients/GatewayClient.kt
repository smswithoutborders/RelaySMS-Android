package com.example.sw0b_001.data.GatewayClients

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["MSISDN"], unique = true)])
class GatewayClient {
    enum class TYPE(val value: String) {
        DEFAULT("default"),
        CUSTOM("custom")
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "type")
    var type: String? = null

    @ColumnInfo(name = "MSISDN")
    var mSISDN: String? = null

    @ColumnInfo(name = "default")
    var isDefault: Boolean = false

    @ColumnInfo(name = "operator_name")
    var operatorName: String? = null

    var alias: String? = null

    @ColumnInfo(name = "operator_id")
    var operatorId: String? = null

    @ColumnInfo(name = "country")
    var country: String? = null

    @ColumnInfo(name = "last_ping_session")
    var lastPingSession: Double = 0.0

    @ColumnInfo(defaultValue = "0")
    var date: Long = System.currentTimeMillis()

    constructor(
        type: String?,
        MSISDN: String?,
        operatorName: String?,
        country: String?,
        isDefault: Boolean
    ) {
        this.type = type
        this.mSISDN = MSISDN
        this.operatorName = operatorName
        this.isDefault = isDefault
        this.country = country
    }

    constructor()

    companion object {
        var TYPE_CUSTOM: String = "custom"
    }
}
