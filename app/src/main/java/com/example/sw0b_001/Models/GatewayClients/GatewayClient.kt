package com.example.sw0b_001.Models.GatewayClients

import android.content.Context
import android.widget.Toast
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.Publisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

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

        fun refreshGatewayClients(context: Context, failedCallback: Runnable) {
            val scope = CoroutineScope(Dispatchers.Default)
            scope.launch {
                try {
                    Publisher.getAvailablePlatforms(context).let{ json ->
                        json.forEach { it->
                            val url = URL(it.icon_png)
                            it.logo = url.readBytes()
                        }
                        Datastore.getDatastore(context).availablePlatformsDao()
                            .fetchAllList().forEach {
                                if(!json.contains(it)) {
                                    Datastore.getDatastore(context).availablePlatformsDao()
                                        .delete(it.name)
                                }
                            }
                        Datastore.getDatastore(context).availablePlatformsDao()
                            .insertAll(json)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                    failedCallback.run()
                }
            }
        }
    }
}
