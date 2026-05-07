package com.example.sw0b_001.extensions.context

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity


fun Context.getAppCompatActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getAppCompatActivity()
    else -> null
}

