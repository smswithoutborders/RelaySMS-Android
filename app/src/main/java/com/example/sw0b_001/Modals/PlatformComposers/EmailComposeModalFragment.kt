package com.example.sw0b_001.Modals.PlatformComposers

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.Bridges
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Platforms._PlatformsHandler
import com.example.sw0b_001.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmailComposeModalFragment(val platform: StoredPlatformsEntity,
                                val message: EncryptedContent? = null,
                                val isBridge: Boolean = false,
                                private val onSuccessCallback: Runnable? = null)
    : BottomSheetDialogFragment(R.layout.fragment_modal_email_compose) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.email_compose_toolbar)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.email_compose_menu_action_send -> {
                    processSend(view)
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }

        val bottomSheet = view.findViewById<View>(R.id.email_compose_constraint)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isDraggable = true

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        message?.let {
            it.encryptedContent.split(":").let {
                if(isBridge) {
                    view.findViewById<TextInputEditText>(R.id.email_to).apply {
                        setText(it[0])
                    }
                    view.findViewById<TextInputEditText>(R.id.email_cc).apply {
                        setText(it[1])
                    }
                    view.findViewById<TextInputEditText>(R.id.email_bcc).apply {
                        setText(it[2])
                    }
                    view.findViewById<TextInputEditText>(R.id.email_subject).apply {
                        setText(it[3])
                    }
                    view.findViewById<EditText>(R.id.email_compose_body_input).apply {
                        setText(it.subList(4, it.size).joinToString())
                    }
                } else {
                    view.findViewById<TextInputEditText>(R.id.email_to).apply {
                        setText(it[1])
                    }
                    view.findViewById<TextInputEditText>(R.id.email_cc).apply {
                        setText(it[2])
                    }
                    view.findViewById<TextInputEditText>(R.id.email_bcc).apply {
                        setText(it[3])
                    }
                    view.findViewById<TextInputEditText>(R.id.email_subject).apply {
                        setText(it[4])
                    }
                    view.findViewById<EditText>(R.id.email_compose_body_input).apply {
                        setText(it.subList(5, it.size).joinToString())
                    }
                }
            }
        }

        view.findViewById<TextInputEditText>(R.id.email_from).apply {
            setText(platform.account)
        }
    }

    private fun processSend(view: View) {
        val toEditText = view.findViewById<TextInputEditText>(R.id.email_to)
        val ccTextInputEditText = view.findViewById<TextInputEditText>(R.id.email_cc)
        val bccTextInputEditText = view.findViewById<TextInputEditText>(R.id.email_bcc)
        val subjectTextInputEditText = view.findViewById<TextInputEditText>(R.id.email_subject)
        val bodyTextInputEditText = view.findViewById<EditText>(R.id.email_compose_body_input)

        if (toEditText.text.isNullOrEmpty()) {
            toEditText.error = getString(R.string.message_compose_empty_recipient)
            return
        }
        if (bodyTextInputEditText.text.isNullOrEmpty()) {
            bodyTextInputEditText.error = getString(R.string.message_compose_empty_body)
            return
        }

        val to = toEditText.text.toString()
        val cc = ccTextInputEditText.text.toString()
        val bcc = bccTextInputEditText.text.toString()
        val subject = subjectTextInputEditText.text.toString()
        val body = bodyTextInputEditText.text.toString()

        CoroutineScope(Dispatchers.Default).launch {
            val availablePlatforms = if(isBridge) Bridges.platforms
            else Datastore.getDatastore(requireContext())
                .availablePlatformsDao().fetch(platform.name!!)
            val formattedContent = processEmailForEncryption(to, cc, bcc, subject, body)

            try {
                ComposeHandlers.compose(requireContext(),
                    formattedContent,
                    availablePlatforms,
                    platform,
                    isBridge = isBridge,
                    authCode = if(isBridge) Bridges.getAuthCode(requireContext())
                        .encodeToByteArray() else null
                ) {
                    onSuccessCallback?.run()
                    dismiss()
                }
            } catch(e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processEmailForEncryption(to: String,
                                          cc: String,
                                          bcc: String,
                                          subject: String,
                                          body: String): String {
        return if(isBridge) "$to:$cc:$bcc:$subject:$body"
        else "${platform.account}:$to:$cc:$bcc:$subject:$body"
    }
}