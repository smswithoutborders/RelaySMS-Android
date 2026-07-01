package com.example.sw0b_001.data.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
@Keep
data class SupportedPlatforms(
    @PrimaryKey
    val name: String,
    val display_name: String,
    var cat_id: Int,
    val proto_id: Int?,
    val icon_svg: String?,
    val icon_png: String?,
    var logo: ByteArray? = null
)

