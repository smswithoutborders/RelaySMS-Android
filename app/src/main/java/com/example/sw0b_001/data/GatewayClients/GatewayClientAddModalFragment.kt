package com.example.sw0b_001.data.GatewayClients

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GatewayClientAddModalFragment(val gatewayClientId: Long? = null) :
    BottomSheetDialogFragment(R.layout.fragment_gateway_client_add_modal) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet = view.findViewById<View>(R.id.gateway_client_add_modal)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        view.findViewById<MaterialButton>(R.id.gateway_client_add_custom_btn)
            .setOnClickListener {
                addGatewayClients(view)
            }

        val textInputLayout = view.findViewById<TextInputLayout>(R.id.gateway_client_add_contact_layout)
        textInputLayout.setEndIconOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            }
            startActivityForResult(intent, 1)
        }

        if (gatewayClientId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val gatewayClient = Datastore.getDatastore(requireContext()).gatewayClientsDao()
                    .fetch(gatewayClientId!!)
                val contactTextView =
                    view.findViewById<TextInputEditText>(R.id.gateway_client_add_contact)
                val aliasTextView =
                    view.findViewById<TextInputEditText>(R.id.gateway_client_add_contact_alias)
                activity?.runOnUiThread {
                    contactTextView.setText(gatewayClient.mSISDN)
                    aliasTextView.setText(gatewayClient.alias)
                }
            }
        }
    }

    private fun addGatewayClients(view: View) {
        val contactTextView = view.findViewById<TextInputEditText>(R.id.gateway_client_add_contact)
        val aliasTextView =
            view.findViewById<TextInputEditText>(R.id.gateway_client_add_contact_alias)

        if (contactTextView.text.isNullOrEmpty()) {
            contactTextView.error =
                getString(R.string.gateway_client_settings_add_custom_empty_error)
            return
        }

        val gatewayClient = GatewayClient()
        gatewayClient.mSISDN = contactTextView.text.toString()
        gatewayClient.alias = aliasTextView.text?.toString()
        gatewayClient.type = GatewayClient.TYPE_CUSTOM

        if (gatewayClientId != null) {
            gatewayClient.id = gatewayClientId
            CoroutineScope(Dispatchers.Default).launch {
                Datastore.getDatastore(context).gatewayClientsDao().update(gatewayClient)
                dismiss()
            }
        } else {
            CoroutineScope(Dispatchers.Default).launch {
                Datastore.getDatastore(context).gatewayClientsDao().insert(gatewayClient)
                dismiss()
            }
        }
    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)

        if (reqCode == 1 && resultCode == RESULT_OK) {
            val contactData = data?.data
            val contactCursor = requireContext().contentResolver.query(
                contactData!!, null, null, null, null
            )
            if (contactCursor != null && contactCursor.moveToFirst()) {
                val numberIndex = contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val number = contactCursor.getString(numberIndex).filter { !it.isWhitespace() }
                view?.findViewById<TextInputEditText>(R.id.gateway_client_add_contact)?.setText(number)

                val nameIndex = contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val name = contactCursor.getString(nameIndex)
                view?.findViewById<TextInputEditText>(R.id.gateway_client_add_contact_alias)?.setText(name)

                contactCursor.close()
            }
        }
    }
}