package com.example.sw0b_001.data.models

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.Publishers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

//@DatabaseView("SELECT platform.name, platform.description, platform.provider, platform.image, platform.id FROM platform")
@Entity(indices = [Index(value = ["name"], unique = true)])
class Platforms {

    enum class ServiceTypes {
        EMAIL,
        TEXT,
        MESSAGE,
        BRIDGE,
        BRIDGE_INCOMING,
        TEST,
    }

    enum class ProtocolTypes{
        oauth2,
        pnba,
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var name: String? = null

    var description: String? = null

    var logo: Long = 0

    var letter: String? = null

    var type: String? = null

    @ColumnInfo(defaultValue = "0")
    var isSaved: Boolean = false

}