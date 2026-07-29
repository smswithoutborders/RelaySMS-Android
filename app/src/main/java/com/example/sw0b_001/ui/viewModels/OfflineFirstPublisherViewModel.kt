package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.data.TransportImpl.publishWithAttachment
import com.example.sw0b_001.data.TransportImpl.publishWithoutAttachment
import com.example.sw0b_001.data.models.Payloads
import com.example.sw0b_001.extensions.context.getStaticKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.relaysms_spec_payload.OfflineFirst
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer

@HiltViewModel
class OfflineFirstPublisherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {

    private val _debugUiState = MutableStateFlow(BuildConfig.DEBUG)
    val debugUiState: StateFlow<Boolean> = _debugUiState

    fun toggleDebugState() { _debugUiState.value = !_debugUiState.value }

    fun publish(
        catId: V1ContentCategories,
        body: String,
        to: String?,
        subject: String?,
        imageViewModel: ImageViewModel,
        payloadsViewModel: PayloadsViewModel,
        onFailureCallback: (String) -> Unit,
        onCompleteCallback: () -> Unit,
    ) {
        val attachment = imageViewModel.processedImage.value?.rawBytes
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var contentContainer: V1ContentsContainer?
                val isAttachment = attachment != null
                try {
                    if(isAttachment) {
                        val sessionId = imageViewModel.getSessionId(context)
                        contentContainer = publishWithAttachment(
                            context,
                            catId,
                            body,
                            tokenId = null,
                            to,
                            subject,
                            attachment,
                            imageViewModel,
                            sessionId = sessionId
                        ) { payload ->
                            encrypt(payload, true)
                        }
                    } else {
                        contentContainer = publishWithoutAttachment(
                            context,
                            catId,
                            body,
                            tokenId = null,
                            to,
                            subject,
                            debugOnly = _debugUiState.value,
                        ) { p ->
                            encrypt(p)
                        }
                    }

                    val payload = Payloads(
                        catId = catId,
                        content = contentContainer,
                        platformName = "RelaySMS", // TODO: match incoming name
                    )

                    payloadsViewModel.insert(payload)

                    withContext(Dispatchers.Main) {
                        onCompleteCallback()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onFailureCallback(e.message ?: "")
                }
            }
        }
    }

    private fun encrypt(
        plaintext: ByteArray,
        withAttachment: Boolean = false,
    ) : Pair<ByteArray, Int> {
        val keyId = if(withAttachment) (0 until 16).random()
        else (0 until 255).random()

        val authenticationPublicKey = context.getStaticKeys(keyId)
            ?: throw Exception("Could not find static keys for id")

        val protocol = Protocols(context)
        protocol.generateDH().use { ec ->
            protocol.generateDH().use { sc ->
                val offlineFirst = OfflineFirst.encrypt(
                    ssPk = authenticationPublicKey,
                    ec = ec.privateKey!!,
                    sc = sc.privateKey!!,
                    payload = plaintext
                )
                return Pair(offlineFirst.serialize(), keyId)
            }
        }

    }
}