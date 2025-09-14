package com.example.sw0b_001.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity
data class AvailablePlatforms(
    @PrimaryKey val name: String,
    @ColumnInfo(name="shortcode") val shortcode: String?,
    @ColumnInfo(name="service_type") var service_type: String?,
    @ColumnInfo(name="protocol_type") val protocol_type: String?,
    @ColumnInfo(name="icon_svg") val icon_svg: String?,
    @ColumnInfo(name="icon_png") val icon_png: String?,
    @ColumnInfo(name="support_url_scheme") val support_url_scheme: Boolean?,

    @Transient
    @ColumnInfo(name="logo", typeAffinity = ColumnInfo.Companion.BLOB)
    var logo: ByteArray? = null
)