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
import androidx.room.Entity
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import uniffi.relaysms_spec_payload.V1PayloadsSupportedProtocols
import uniffi.relaysms_spec_payload.v1PayloadSupportProtocolsFromU8

sealed class TokensUiState {
    object Loading: TokensUiState()
    object Idle: TokensUiState()
    data class Success(
        val url: Uri?,
    ): TokensUiState()
    data class Error(val exception: Throwable): TokensUiState()
}

@Entity
@Serializable
data class TokensMetrics(
    val account: String,
    val quantityEncryptionKeysClient: Int,
    val quantityEncryptionKeysServer: Int,
    val quantityText: Int,
    val quantityAttachments: Int,
    val lastSync: Long,
    val date: Long
)


sealed class PnbaUiState {
    object PhoneNumberRequested : PnbaUiState()
    object AuthCodeRequested: PnbaUiState()
    object PasswordRequested : PnbaUiState()
    object Success : PnbaUiState()
    data class Error(val message: String) : PnbaUiState()
}

@HiltViewModel
class TokensViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var _selectedToken: MutableStateFlow<Tokens?> = MutableStateFlow(null)
    var selectedToken: StateFlow<Tokens?> = _selectedToken.asStateFlow()

    fun updateSelectedToken(token: Tokens) {
        _selectedToken.value = token
    }

    var bottomTabsItem by mutableStateOf(BottomTabsItems.BottomBarRecentTab)

    private val _isStoringUiState =
        MutableStateFlow<TokensUiState>(TokensUiState.Success(null))
    val isStoringUiState: StateFlow<TokensUiState> = _isStoringUiState

    private val _isRevokingUiState =
        MutableStateFlow<TokensUiState>(TokensUiState.Success(null))
    val isRevokingUiState: StateFlow<TokensUiState> = _isRevokingUiState

    private val _pnbaUiState =
        MutableStateFlow<PnbaUiState>(PnbaUiState.PhoneNumberRequested)
    val pnbaUiState: StateFlow<PnbaUiState> = _pnbaUiState

    private val _deletingUiState =
        MutableStateFlow<TokensUiState>(TokensUiState.Idle)
    val deletingUiState: StateFlow<TokensUiState> = _deletingUiState

    // Selection mode properties
    var isSelectionMode by mutableStateOf(false)
    var selectedMessagesCount by mutableIntStateOf(0)
    var onSelectAll: (() -> Unit)? = null
    var onDeleteSelected: (() -> Unit)? = null
    var onCancelSelection: (() -> Unit)? = null

    private val db = Datastore.getDatastore(context)?.tokensDao()
        ?: throw Exception("Cannot open database")

    fun deleteAll() {
        _selectedToken.value = null
        _pnbaUiState.value = PnbaUiState.PhoneNumberRequested
        _isStoringUiState.value = TokensUiState.Idle
    }

    fun get(): Flow<List<Tokens>> {
        return db.fetchAll()
    }

    fun fetchTokensForPlatforms(platformName: String): Flow<List<Tokens>> {
        return db.fetch(platformName)
    }

    fun fetchTokenMetrics(tokenId: Long): Flow<TokensMetrics> {
        return db.getTokensMetrics(
            tokenId,
            PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_SERVER_ATTACHMENT,
            aliasClient = PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_CLIENT,
            aliasServer = PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_SERVER,
        )
    }

    fun deleteAll(onCompleteCallback: ()-> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _deletingUiState.value = TokensUiState.Loading
            val platforms = Datastore.getDatastore(context)
                ?.supportedPlatformsCacheDao()
                ?.fetchList()
                ?: throw Exception("Failed to open supported platforms db")
            db.fetchAllList().forEach { token ->
                try {
                    revokeAll(
                        platforms.find { it.name == token.platformName }!!,
                        token,
                    )
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }

            withContext(Dispatchers.Main) {
                _deletingUiState.value = TokensUiState.Success(null)
                onCompleteCallback()
            }
        }
    }

    private fun revokeAll(
        platform: SupportedPlatforms,
        account: Tokens,
    ) {
        PublisherGrpcImpl(context).use { publisherGrpcImpl ->
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
            } catch(e: Exception) {
                e.printStackTrace()
                throw e
            }
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

    fun storeCustom(
        platform: SupportedPlatforms,
        phoneNumber: String? = null,
        authCode: String? = null,
        password: String? = null,
        channel: String? = null,
        onCompleteCallback: (Pair<Boolean, String?>, Long?) -> Unit
    ){
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val expiry = triggerPNBARequested(
                    phoneNumber = phoneNumber!!,
                    platform = platform,
                    authCode = authCode,
                    password = password,
                    channel = channel,
                )
                onCompleteCallback(Pair(true, null), expiry)
            } catch (e: Exception) {
                e.printStackTrace()
                onCompleteCallback(Pair(false, e.message), null)
            }
        }
    }

    fun store(
        platform: SupportedPlatforms,
        phoneNumber: String? = null,
        authCode: String? = null,
        password: String? = null,
        channel: String? = null,
        onFailedCallback: (String) -> Unit,
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
                withContext(Dispatchers.Main) {
                    onFailedCallback(e.message ?: "")
                }
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

    fun setPnbaState(state: PnbaUiState) {
        _pnbaUiState.value = state
    }

    private fun triggerPNBARequested(
        phoneNumber: String,
        platform: SupportedPlatforms,
        authCode: String? = null,
        password: String? = null,
        channel: String? = null,
    ) : Long? {
        PublisherGrpcImpl(context).use { publisherGrpcImpl ->
            try {
                when(val state = _pnbaUiState.value) {
                    PnbaUiState.PhoneNumberRequested -> {
                        val res = publisherGrpcImpl.phoneNumberBaseAuthenticationRequest(
                            phoneNumber,
                            platform.name,
                            channel,
                        )
                        _pnbaUiState.value = PnbaUiState.AuthCodeRequested
                        _isStoringUiState.value = TokensUiState.Success( null, )
                        return res.expiresAt
                    }
                    PnbaUiState.AuthCodeRequested -> {
                        try {
                            val res = publisherGrpcImpl.phoneNumberBaseAuthenticationExchange(
                                authorizationCode = authCode!!,
                                phoneNumber = phoneNumber,
                                platform = platform.name,
                                channel = channel
                            )
                            if(res == null) {
                                _pnbaUiState.value = PnbaUiState.Success
                            } else {
                                _pnbaUiState.value = PnbaUiState.PasswordRequested
                            }
                        } catch(e: Exception) {
                            throw if(e is io.grpc.StatusRuntimeException) {
                                Exception(context.getString(R.string.wrong_code))
                            } else e
                        }
                    }
                    PnbaUiState.PasswordRequested -> {
                        publisherGrpcImpl.phoneNumberBaseAuthenticationExchange(
                            authorizationCode = authCode!!,
                            phoneNumber = phoneNumber,
                            platform = platform.name,
                            password = password!!,
                            channel = channel
                        )
                        _pnbaUiState.value = PnbaUiState.Success
                    }
                    else -> {}
                }
                _isStoringUiState.value = TokensUiState.Success( null, )
            } catch(e: Exception) {
                e.printStackTrace()
                if(_pnbaUiState.value == PnbaUiState.AuthCodeRequested) {
                    throw e
                }
                else _isStoringUiState.value = TokensUiState.Error(e)
            }
        }
        return null
    }

    fun refreshTokens(tokenId: Long) {
        _isStoringUiState.value = TokensUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            PublisherGrpcImpl(context).use { publisherGrpcImpl ->
                try {
                    val token = db.fetch(tokenId)
                    token.use {
                        publisherGrpcImpl.refreshKeys(token)
                        _isStoringUiState.value = TokensUiState.Success(null)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                    _isStoringUiState.value = TokensUiState.Error(e)
                }
            }
        }
    }
}