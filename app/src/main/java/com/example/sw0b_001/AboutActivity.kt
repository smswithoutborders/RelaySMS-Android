package com.example.sw0b_001

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar = findViewById<View>(R.id.about_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val xIcon = findViewById<ImageView>(R.id.x_icon)
        val githubIcon = findViewById<ImageView>(R.id.github_link_icon)

        xIcon.setOnClickListener {
            openSocialLink("https://x.com/RelaySMS")
        }

        githubIcon.setOnClickListener {
            openSocialLink("https://github.com/smswithoutborders/SMSWithoutBorders-App-Android")
        }

        val viewOnGithub = findViewById<TextView>(R.id.view_on_github)
        viewOnGithub.setOnClickListener {
            openSocialLink("https://github.com/smswithoutborders/SMSWithoutBorders-App-Android")
        }

        val tutorialButton = findViewById<Button>(R.id.tutorial_button)
        tutorialButton.setOnClickListener {
            openSocialLink("https://docs.smswithoutborders.com/docs/App%20Tutorial/New-Tutorial")
        }

        val appTitleTextView = findViewById<TextView>(R.id.app_title)
        appTitleTextView.text = getString(R.string.app_name)

        val appVersionTextView = findViewById<TextView>(R.id.app_version)
        appVersionTextView.text = BuildConfig.VERSION_NAME
    }

    private fun openSocialLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

}