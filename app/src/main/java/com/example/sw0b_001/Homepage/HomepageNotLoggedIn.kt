package com.example.sw0b_001.Homepage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.sw0b_001.Bridges.BridgesSubmitCodeFragment
import com.example.sw0b_001.Modals.BridgesAuthRequestModalFragment
import com.example.sw0b_001.Modals.LoginModalFragment
import com.example.sw0b_001.Modals.SignupModalFragment
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

        val verifyCodeRunnable = Runnable {
            activity?.supportFragmentManager?.commit {
                replace<BridgesSubmitCodeFragment>(R.id.homepage_fragment_container)
                addToBackStack(null)
            }
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

        view.findViewById<MaterialButton>(R.id.homepage_bridges_auth_btn).setOnClickListener {
            val bridgesAuthModalFragment = BridgesAuthRequestModalFragment(verifyCodeRunnable)
            bridgesAuthModalFragment.show(parentFragmentManager, "bridges_auth_tag")
        }
    }
}