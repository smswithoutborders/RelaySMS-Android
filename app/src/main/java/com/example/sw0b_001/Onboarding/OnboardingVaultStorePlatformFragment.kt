package com.example.sw0b_001.Onboarding

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.transition.TransitionInflater
import com.example.sw0b_001.Modals.AvailablePlatformsModalFragment
import com.example.sw0b_001.Modals.LoginModalFragment
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.R
import com.google.android.material.button.MaterialButton

class OnboardingVaultStorePlatformFragment:
        OnboardingComponent(R.layout.fragment_onboarding_vault_store){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginSuccessRunnable = Runnable {
            activity?.runOnUiThread {
                showPlatformsModal()
            }
        }

        view.findViewById<MaterialButton>(R.id.onboarding_welcome_vaults_store_description_try_example_btn)
            .setOnClickListener {
                if (Vaults.fetchLongLivedToken(requireContext()).isNotBlank()) {
                    showPlatformsModal()
                } else {
                    val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
                    val loginModalFragment = LoginModalFragment(loginSuccessRunnable)
                    fragmentTransaction?.add(loginModalFragment, "login_signup_login_vault_tag")
                    fragmentTransaction?.show(loginModalFragment)
                    fragmentTransaction?.commit()
                }
            }
    }

    private fun showPlatformsModal() {
        val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
        val platformsModalFragment = AvailablePlatformsModalFragment(
            AvailablePlatformsModalFragment.Type.AVAILABLE)
        fragmentTransaction?.add(platformsModalFragment, "store_platforms_tag")
        fragmentTransaction?.show(platformsModalFragment)
        activity?.runOnUiThread { fragmentTransaction?.commit() }
    }
}