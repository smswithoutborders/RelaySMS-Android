package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.extensions.generateRandomBytes
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.grpc.PublisherGrpcImpl
import com.example.sw0b_001.data.models.SupportedPlatforms
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.ui.views.tabs.BottomTabsItems
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1PayloadsSupportedProtocols
import uniffi.relaysms_spec_payload.v1PayloadSupportProtocolsFromU8

sealed class TokensUiState {
    object Loading: TokensUiState()
    data class Success(
        val url: Uri?,
        val pnbaAuthRequired: Boolean = false,
        val pnbaPasswordRequired: Boolean = false,
    ): TokensUiState()
    data class Error(val exception: Throwable): TokensUiState()
}

@HiltViewModel
class TokensViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    var bottomTabsItem by mutableStateOf(BottomTabsItems.BottomBarRecentTab)

    private val _isStoringUiState =
        MutableStateFlow<TokensUiState>(TokensUiState.Success(null))
    val isStoringUiState: StateFlow<TokensUiState> = _isStoringUiState

    private val _isRevokingUiState =
        MutableStateFlow<TokensUiState>(TokensUiState.Success(null))
    val isRevokingUiState: StateFlow<TokensUiState> = _isRevokingUiState

    // Selection mode properties
    var isSelectionMode by mutableStateOf(false)
    var selectedMessagesCount by mutableIntStateOf(0)
    var onSelectAll: (() -> Unit)? = null
    var onDeleteSelected: (() -> Unit)? = null
    var onCancelSelection: (() -> Unit)? = null

    private val db = Datastore.getDatastore(context)?.tokensDao()
        ?: throw Exception("Cannot open database")

    private val cache = Datastore.getDatastore(context)?.supportedPlatformsCacheDao()
        ?: throw Exception("Cannot open database")

    fun clearStoringState() {
        _isStoringUiState.value = TokensUiState.Success(null)
    }

    fun get(): Flow<List<Tokens>> {
        return db.fetchAll()
    }

    fun fetchTokensByCatId(catId: V1ContentCategories): Flow<List<Tokens>> {
        return db.fetchCatId(catId)
    }

    fun reset(onCompleteCallback: ()-> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                db.deleteAll()
            }
            onCompleteCallback()
        }
    }

    companion object {
        fun parseLocalImageContent(
            content: ByteArray,
            imageLength: Int,
            textLength: Int,
        ) : Pair<ByteArray, ByteArray> {
//            var content = Base64.decode(content, Base64.DEFAULT)
            var content = content
            val image = content.take(imageLength).toByteArray().also {
                content = content.drop(imageLength).toByteArray() }
            val text = content.take(textLength).toByteArray()

            return Pair(image, text)
        }

        fun oAuth2IntentBuilder(context: Context): CustomTabsIntent {
            // get the current toolbar background color (this might work differently in your app)
            @ColorInt val colorPrimaryLight = ContextCompat.getColor( context,
                R.color.md_theme_primary)

            return CustomTabsIntent.Builder() // set the default color scheme
                .setSendToExternalDefaultHandlerEnabled(true)
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(colorPrimaryLight)
                        .build()
                )
                .setStartAnimations(context,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right)
                .setExitAnimations(context,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right)
                .build()
        }

        class MutableStateSerializer<T>(
            private val valueSerializer: KSerializer<T>
        ) : KSerializer<MutableState<T>> {

            override val descriptor: SerialDescriptor =
                valueSerializer.descriptor

            override fun serialize(encoder: Encoder, value: MutableState<T>) {
                valueSerializer.serialize(encoder, value.value)
            }

            override fun deserialize(decoder: Decoder): MutableState<T> {
                return mutableStateOf(valueSerializer.deserialize(decoder))
            }
        }
    }

    fun revoke(
        platform: SupportedPlatforms,
        account: Tokens,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            PublisherGrpcImpl(context).use { publisherGrpcImpl ->
                _isRevokingUiState.value = TokensUiState.Loading
                try {
                    when(v1PayloadSupportProtocolsFromU8(
                        platform.proto_id!!.toUByte())) {
                        V1PayloadsSupportedProtocols.O_AUTH20 -> {
                            publisherGrpcImpl.revokeOAuth2(account)
                        }
                        V1PayloadsSupportedProtocols.PNBA -> {
                            publisherGrpcImpl.revokePnba(account) }
                    }
                    db.delete(account)
                    _isRevokingUiState.value = TokensUiState.Success(null)
                } catch(e: Exception) {
                    e.printStackTrace()
                    _isRevokingUiState.value = TokensUiState.Error(e)
                }
            }
        }
    }

    fun store(
        platform: SupportedPlatforms,
        phoneNumber: String? = null,
        authCode: String? = null,
        password: String? = null,
        channel: String? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isStoringUiState.value = TokensUiState.Loading
            try {
                when(v1PayloadSupportProtocolsFromU8(platform.proto_id!!.toUByte())) {
                    V1PayloadsSupportedProtocols.O_AUTH20 -> {
                        triggerOAuthRequested(platform)
                    }
                    V1PayloadsSupportedProtocols.PNBA -> {
                        triggerPNBARequested(
                            phoneNumber = phoneNumber!!,
                            platform = platform,
                            authCode = authCode,
                            password = password,
                            channel = channel,
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isStoringUiState.value = TokensUiState.Error(e)
            }
        }
    }

    private fun triggerOAuthRequested(
        platform: SupportedPlatforms,
    ) {
        PublisherGrpcImpl(context).use { publisherGrpcImpl ->
            val requestId = context.generateRandomBytes(32);
            val requestIdentifier = Base64.encodeToString(
                requestId, Base64.NO_WRAP)
            try {
                val response = publisherGrpcImpl.getOAuthURL(
                    availablePlatforms = platform,
                    autogenerateCodeVerifier = true,
                    supportsUrlScheme = false,
                    requestIdentifier = requestIdentifier
                )

                PublisherGrpcImpl.storeOauthRequestCodeVerifier(
                    context,
                    platform.name,
                    response.codeVerifier.toByteArray(),
                    requestId
                )

                val intentUri = response.authorizationUrl.toUri()
                _isStoringUiState.value = TokensUiState.Success(intentUri)
            } catch(e: Exception) {
                throw e
            } finally {
                requestId.fill(0)
            }
        }
    }

    private suspend fun triggerPNBARequested(
        phoneNumber: String,
        platform: SupportedPlatforms,
        authCode: String? = null,
        password: String? = null,
        channel: String? = null,
    ) {
        PublisherGrpcImpl(context).use { publisherGrpcImpl ->
            try {
                when {
                    !authCode.isNullOrEmpty() && !password.isNullOrEmpty() -> {
                        publisherGrpcImpl.phoneNumberBaseAuthenticationExchange(
                            authorizationCode = authCode,
                            phoneNumber = phoneNumber,
                            platform = platform.name,
                            password = password,
                            channel = channel
                        )
                        _isStoringUiState.value = TokensUiState.Success(null)
                    }
                    !authCode.isNullOrEmpty() -> {
                        val res = publisherGrpcImpl.phoneNumberBaseAuthenticationExchange(
                            authorizationCode = authCode,
                            phoneNumber = phoneNumber,
                            platform = platform.name,
                            channel = channel
                        )
                        _isStoringUiState.value = TokensUiState.Success(
                            null,
                            pnbaPasswordRequired = res.twoStepVerificationEnabled,
                        )
                    }
                    else -> {
                        publisherGrpcImpl.phoneNumberBaseAuthenticationRequest(
                            phoneNumber,
                            platform.name,
                            channel,
                        )
                        _isStoringUiState.value = TokensUiState.Success(
                            null ,
                            pnbaAuthRequired = true
                        )
                    }
                }
            } catch(e: Exception) {
                throw e
            }
        }
    }
}