package com.example.sw0b_001.Models.Platforms

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["account", "name"], unique = true)])
data class StoredPlatformsEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name="account") val account: String?,
    @ColumnInfo(name="name") val name: String?,
    @ColumnInfo(
        name="access_token",
        defaultValue = ""
    ) var accessToken: String?,
    @ColumnInfo(
        name="refresh_token",
        defaultValue = ""
    ) var refreshToken: String?
)