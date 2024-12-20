package com.example.sw0b_001.Homepage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.example.sw0b_001.Bridges.BridgesSubmitCodeActivity
import com.example.sw0b_001.Modals.BridgesAuthRequestModalFragment
import com.example.sw0b_001.Modals.LoginModalFragment
import com.example.sw0b_001.Modals.PlatformComposers.EmailComposeModalFragment
import com.example.sw0b_001.Modals.SignupModalFragment
import com.example.sw0b_001.Models.Bridges
import com.example.sw0b_001.R
import com.google.android.material.button.MaterialButton

class HomepageNotLoggedIn : Fragment(R.layout.fragment_homepage_not_logged_in) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginSuccessRunnable = Runnable {
            activity?.recreate()
        }

        view.findViewById<MaterialButton>(R.id.homepage_vault_signup_btn).setOnClickListener {
            val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
            val signupModalFragment = SignupModalFragment(loginSuccessRunnable)
            fragmentTransaction?.add(signupModalFragment, "signup_tag")
            fragmentTransaction?.show(signupModalFragment)
            fragmentTransaction?.commit()
        }

        view.findViewById<MaterialButton>(R.id.homepage_vault_login_btn).setOnClickListener {
            val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
            val loginModalFragment = LoginModalFragment(loginSuccessRunnable)
            fragmentTransaction?.add(loginModalFragment, "login_signup_login_vault_tag")
            fragmentTransaction?.show(loginModalFragment)
            fragmentTransaction?.commit()
        }

    }

    override fun onResume() {
        super.onResume()
        requireView().findViewById<MaterialButton>(R.id.homepage_bridges_auth_btn).apply {
            val canPublish = Bridges.canPublish(requireContext())
            if(canPublish) {
                this.text = context.getString(R.string.send_message_without_an_account)
                this.setBackgroundColor(resources.getColor(R.color.md_theme_primary,
                    requireContext().theme))
                this.setTextColor(resources.getColor(R.color.md_theme_onPrimary,
                    requireContext().theme))
            }
            setOnClickListener {
                val bridgesAuthModalFragment = BridgesAuthRequestModalFragment(canPublish) {
                    if(canPublish) {
                        val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
                        val emailComposeModalFragment = EmailComposeModalFragment(
                            Bridges.storedPlatformsEntity,
                            isBridge = true
                        ) {
//                            activity?.finish()
                        }
                        fragmentTransaction?.add(emailComposeModalFragment, "email_compose_tag")
                        fragmentTransaction?.show(emailComposeModalFragment)
                        fragmentTransaction?.commitNow()
                    }
                    else {
                        startActivity(Intent(requireContext(),
                            BridgesSubmitCodeActivity::class.java))
                    }
                }
                bridgesAuthModalFragment.show(parentFragmentManager, "bridges_auth_tag")
            }
        }
    }
}