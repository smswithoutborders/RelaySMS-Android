package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.example.sw0b_001.data.TransportImpl.publishWithAttachment
import com.example.sw0b_001.data.TransportImpl.publishWithoutAttachment
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.relaysms_spec_payload.V1ContentCategories

@HiltViewModel
class BridgesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    companion object {
        init {
            System.loadLibrary("librelaysms_spec_payload")
        }
    }

    fun publish(
        body: String,
        tokenId: Int?,
        to: String?,
        subject: String?,
        attachment: ImageViewModel.ProcessedImage?,
        onFailureCallback: (String) -> Unit,
        onCompleteCallback: () -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if(attachment != null) {
                        publishWithAttachment(
                            V1ContentCategories.BRIDGE,
                            body,
                            tokenId,
                            to,
                            subject,
                            attachment.rawBytes!!
                        ) { payload ->
                            encrypt(tokenId!!, payload)
                        }
                    } else {
                        publishWithoutAttachment(
                            V1ContentCategories.BRIDGE,
                            body,
                            tokenId,
                            to,
                            subject,
                        ) { payload ->
                            encrypt(tokenId!!, payload)
                        }
                    }

                    onCompleteCallback()
                } catch (e: Exception) {
                    onFailureCallback(e.message ?: "")
                }
            }
        }
    }

    private fun encrypt(
        tokenId: Int,
        plaintext: ByteArray,
    ) : Pair<ByteArray, Int> {
        TODO()
    }
}