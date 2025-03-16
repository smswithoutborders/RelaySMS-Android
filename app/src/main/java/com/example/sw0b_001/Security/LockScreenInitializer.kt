package com.example.sw0b_001.Security

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.startup.Initializer

class LockScreenInitializer : Initializer<AppCompatActivity> {
    override fun create(context: Context): AppCompatActivity {
//        context.startActivity( Intent(context, LockScreenActivity::class.java).apply {
////            setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//            setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
//        })
        return LockScreenActivity()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}