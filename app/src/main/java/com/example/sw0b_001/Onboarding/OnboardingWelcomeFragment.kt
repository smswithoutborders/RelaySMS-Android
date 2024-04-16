package com.example.sw0b_001.Onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sw0b_001.R

class OnboardingWelcomeFragment : OnboardingComponent() {
    init {
        nextButtonText = "Skip Tour"
        previousButtonText = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_onboarding_welcome, container, false)
    }

    companion object {
        val TAG = "WELCOME_FRAGMENT_TAG"
    }
}