package com.example.sw0b_001.Bridges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.sw0b_001.R

class BridgesSubmitCodeFragment(private val onSuccessRunnable: Runnable? = null) : Fragment(R.layout.fragment_bridges_auth_code) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val infoBoxTextView = view.findViewById<TextView>(R.id.telegram_info_text_view)
        infoBoxTextView.text = "Once you send an authentication request via SMS, you will receive a message containing the code. Copy the entire SMS without adding or removing anything from it and paste it here"
    }
}