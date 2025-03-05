package com.example.sw0b_001.Models.Platforms

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

//@DatabaseView("SELECT platform.name, platform.description, platform.provider, platform.image, platform.id FROM platform")
@Entity(indices = [Index(value = ["name"], unique = true)])
class Platforms {

    enum class ServiceTypes(val type: String) {
        EMAIL("email"),
        TEXT("text"),
        MESSAGE("message"),
        BRIDGE("bridge")
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

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Platforms> =
            object : DiffUtil.ItemCallback<Platforms>() {
                override fun areItemsTheSame(oldItem: Platforms, newItem: Platforms): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: Platforms, newItem: Platforms): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
