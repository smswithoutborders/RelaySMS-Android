package com.example.sw0b_001.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["account", "name"], unique = true)])
data class Accounts(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val account: String,
    val name: String,
)