package com.example.sw0b_001

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sw0b_001.Settings.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar = findViewById<View>(R.id.settings_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportActionBar!!.title = getString(R.string.settings_settings)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_fragment, SettingsFragment())
                .commit()
    }
}