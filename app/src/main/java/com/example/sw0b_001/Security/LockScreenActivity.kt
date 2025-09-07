package com.example.sw0b_001.Security

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.example.sw0b_001.MainActivity
import com.example.sw0b_001.R

class LockScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lock_screen)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if(prefs.getBoolean("lock_screen_always_on", false)) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()

            val lockScreenFragment = LockScreenFragment(
                    successRunnable = { boot() },
                    failedRunnable = null,
                    errorRunnable = { finish() })
            fragmentTransaction.add(lockScreenFragment, "lock_screen_frag_tag")
            fragmentTransaction.show(lockScreenFragment)
            fragmentTransaction.commitNow()
        } else {
            boot()
        }
    }

    private fun boot() {
        val intent = Intent(this, MainActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }
}