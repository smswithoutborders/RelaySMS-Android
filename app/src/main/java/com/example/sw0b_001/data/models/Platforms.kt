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

    enum class ProtocolTypes(val type: String) {
        OAUTH2("oauth2"),
        PNBA("pnba"),
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

    constructor()
    constructor(id: Long) {
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (other is Platforms) {
            return (other.id == this.id &&
                    other.description == description &&
                    other.name == name &&
                    other.type == type &&
                    other.letter == letter)
        }
        return false
    }

    companion object {
        fun refreshAvailablePlatforms(context: Context) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    Publishers.Companion.getAvailablePlatforms(context).let{ json ->
                        json.forEach { it->
                            if(it.icon_png?.isNotEmpty() == true) {
                                val url = URL(it.icon_png)
                                it.logo = url.readBytes()
                            }
                        }
                        Datastore.getDatastore(context).availablePlatformsDao().clear()
                        Datastore.getDatastore(context).availablePlatformsDao().insertAll(json)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }
}