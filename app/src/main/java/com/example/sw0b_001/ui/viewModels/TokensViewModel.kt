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
import com.example.sw0b_001.data.Network
import com.example.sw0b_001.data.grpc.PublisherGrpcImpl
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.ui.views.compose.GatewayClientRequest
import com.example.sw0b_001.ui.views.tabs.BottomTabsItems
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import uniffi.relaysms_spec_payload.V1ContentCategories

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

    private val _storedTokensUiState = MutableStateFlow<List<Tokens>>(emptyList())
    val storedTokensUiState: StateFlow<List<Tokens>> = _storedTokensUiState

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

    fun get() {
        viewModelScope.launch(Dispatchers.IO) {
            db.fetchAll().collect { tokens ->
                _storedTokensUiState.value = tokens
            }
        }
    }

    fun fetchTokensByCatId(catId: V1ContentCategories) {
        viewModelScope.launch(Dispatchers.IO) {
            db.fetchCatId(catId).collect { tokens ->
                _storedTokensUiState.value = tokens
            }
        }
    }

    suspend fun revokeAll() {
        val publisherGrpcImpl = PublisherGrpcImpl(context)
        db.fetchAllList().forEach { sp ->
            val cachedPlatform = cache.fetch(sp.platformName)
            when(cachedPlatform?.protocol_type) {
                "oauth2" -> {
                    publisherGrpcImpl.revokeOAuthPlatforms(
                        sp.platformName,
                        sp.account,
                    )
                }
                "pnba" -> {
                    publisherGrpcImpl.revokePNBAPlatforms(
                        sp.platformName,
                        sp.account
                    )
                }
            }
        }

    }


    companion object {
        const val ITP_VERSION_VALUE: Byte = 0x04

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


        fun networkRequest(
            url: String,
            payload: GatewayClientRequest,
        ) : String? {
            var payload = Json.encodeToString(payload)

            try {
                var response = Network.jsonRequestPost(url, payload)
                var text = response.result.get()
                return text
            } catch(e: Exception) {
                return e.message
            }
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
                    when(platform.protocol_type) {
                        "oauth2" -> {
                            publisherGrpcImpl.revokeOAuthPlatforms(
                                account.platformName,
                                account.account,
                            )
                        }
                        "pnba" -> {
                            publisherGrpcImpl.revokePNBAPlatforms(
                                account.platformName,
                                account.account
                            )
                        }
                    }

                    TODO()
//                    db.delete(account.id)
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
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isStoringUiState.value = TokensUiState.Loading
            if(platform.protocol_type == "oauth2") {
                triggerOAuthRequested(platform)
            }
            else if (platform.protocol_type == "pnba") {
                triggerPNBARequested(
                    phoneNumber = phoneNumber!!,
                    platform = platform,
                    authCode = authCode,
                    password = password
                )
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
                    supportsUrlScheme = platform.support_url_scheme!!,
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
                e.printStackTrace()
                _isStoringUiState.value = TokensUiState.Error(e)
            } finally {
                requestId.fill(0)
            }
        }
    }

    private fun triggerPNBARequested(
        phoneNumber: String,
        platform: SupportedPlatforms,
        authCode: String? = null,
        password: String? = null,
    ) {
        PublisherGrpcImpl(context).use { publisherGrpcImpl ->
            try {
                when {
                    !authCode.isNullOrEmpty() && !password.isNullOrEmpty() -> {
                        val response = publisherGrpcImpl.phoneNumberBaseAuthenticationExchange(
                            authorizationCode = authCode,
                            phoneNumber = phoneNumber,
                            platform = platform.name,
                            password = password
                        )
                        if(response.success) {
                            _isStoringUiState.value = TokensUiState.Success(null)
                        }
                    }
                    !authCode.isNullOrEmpty() -> {
                        val response = publisherGrpcImpl.phoneNumberBaseAuthenticationExchange(
                            authorizationCode = authCode,
                            phoneNumber = phoneNumber,
                            platform = platform.name
                        )
                        if(response.success) {
                            _isStoringUiState.value = TokensUiState.Success(
                                null,
                                pnbaPasswordRequired = response.twoStepVerificationEnabled,
                            )
                        }
                    }
                    else -> {
                        val response = publisherGrpcImpl.phoneNumberBaseAuthenticationRequest(
                            phoneNumber,
                            platform.name
                        )
                        if(response.success) {
                            _isStoringUiState.value = TokensUiState.Success(
                                null ,
                                pnbaAuthRequired = true
                            )
                        }
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }
}