package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SupportedPlatforms(
    @PrimaryKey
    val name: String,
    var cat_id: Int,
    val proto_id: Int?,
    val icon_svg: String?,
    val icon_png: String?,
    val support_url_scheme: Boolean?,
    var logo: ByteArray? = null
)

