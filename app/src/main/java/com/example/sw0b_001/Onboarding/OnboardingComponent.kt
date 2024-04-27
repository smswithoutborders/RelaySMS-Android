package com.example.sw0b_001.Onboarding

import androidx.fragment.app.Fragment

open class OnboardingComponent(val layout: Int) : Fragment(layout) {
    var nextButtonText: String = ""
    var previousButtonText: String = ""
    var skipButtonText: String = ""
    var skipOnboardingFragment: OnboardingComponent? = null

    public interface ManageComponentsListing {
        fun removeComponent(index: Int)
        fun removeComponent(component: OnboardingComponent)
    }
}