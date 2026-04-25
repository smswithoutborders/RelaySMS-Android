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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.Network
import com.example.sw0b_001.data.grpc.PublisherGrpcImpl
import com.example.sw0b_001.data.grpc.VaultsGrpcImpl
import com.example.sw0b_001.data.models.Accounts
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.ui.views.BottomTabsItems
import com.example.sw0b_001.ui.views.compose.GatewayClientRequest
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

sealed class AccountUiState {
    object Loading: AccountUiState()
    data class Success(val url: Uri?): AccountUiState()
    data class Error(val exception: Throwable): AccountUiState()
}

@HiltViewModel
class AccountsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var storedLiveData: LiveData<List<Accounts>> = MutableLiveData()

    var bottomTabsItem by mutableStateOf(BottomTabsItems.BottomBarRecentTab)

    private val _isStoringUiState =
        MutableStateFlow<AccountUiState>(AccountUiState.Success(null))
    val isStoringUiState: StateFlow<AccountUiState> = _isStoringUiState

    private val _isRevokingUiState =
        MutableStateFlow<AccountUiState>(AccountUiState.Success(null))
    val isRevokingUiState: StateFlow<AccountUiState> = _isRevokingUiState

    // Selection mode properties
    var isSelectionMode by mutableStateOf(false)
    var selectedMessagesCount by mutableIntStateOf(0)
    var onSelectAll: (() -> Unit)? = null
    var onDeleteSelected: (() -> Unit)? = null
    var onCancelSelection: (() -> Unit)? = null

    private val db = Datastore.getDatastore(context)?.storedPlatformsDao()
        ?: throw Exception("Cannot open database")

    private val cache = Datastore.getDatastore(context)?.supportedPlatformsCacheDao()
        ?: throw Exception("Cannot open database")

    fun get(): LiveData<List<Accounts>> {
        if(storedLiveData.value.isNullOrEmpty()) {
            storedLiveData = db.fetchAll()
        }
        return storedLiveData
    }

    suspend fun revokeAll() {
        val publisherGrpcImpl = PublisherGrpcImpl(context)
        db.fetchAllList().forEach { sp ->
            val cachedPlatform = cache.fetch(sp.name)
            when(cachedPlatform?.protocol_type) {
                Platforms.ProtocolTypes.oauth2.name -> {
                    publisherGrpcImpl.revokeOAuthPlatforms(
                        sp.name,
                        sp.account,
                    )
                }
                Platforms.ProtocolTypes.pnba.name -> {
                    publisherGrpcImpl.revokePNBAPlatforms(
                        sp.name,
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
        account: Accounts,
    ) {
        viewModelScope.launch {
            PublisherGrpcImpl(context).use { publisherGrpcImpl ->
                _isRevokingUiState.value = AccountUiState.Loading
                try {
                    when(platform.protocol_type) {
                        Platforms.ProtocolTypes.oauth2.name -> {
                            publisherGrpcImpl.revokeOAuthPlatforms(
                                account.name,
                                account.account,
                            )
                        }
                        Platforms.ProtocolTypes.pnba.name -> {
                            publisherGrpcImpl.revokePNBAPlatforms(
                                account.name,
                                account.account
                            )
                        }
                    }

                    db.delete(account.id)
                    _isRevokingUiState.value = AccountUiState.Success(null)
                } catch(e: Exception) {
                    e.printStackTrace()
                    _isRevokingUiState.value = AccountUiState.Error(e)
                }
            }
        }
    }

    fun store(platform: SupportedPlatforms) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = Datastore.getDatastore(context)?.keysDao()
                ?: throw Exception("Could not open database")

            val publisherPublicKey = db
                .fetchPublicKey(VaultsGrpcImpl.clientVaultHandshakeKeystoreAliasStaticKeys)
                ?: throw Exception("Missing private key in credentials for signing")

            _isStoringUiState.value = AccountUiState.Loading
            if(platform.protocol_type == Platforms.ProtocolTypes.oauth2.name) {
                PublisherGrpcImpl(context).use { publisherGrpcImpl ->
                    val requestIdentifier = Base64.encodeToString(
                        publisherPublicKey, Base64.NO_WRAP)
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
                            response.codeVerifier.toByteArray()
                        )

                        val intentUri = response.authorizationUrl.toUri()
                        _isStoringUiState.value = AccountUiState.Success(intentUri)
                    } catch(e: Exception) {
                        e.printStackTrace()
                        _isStoringUiState.value = AccountUiState.Error(e)
                    }
                }
            }
        }

    }


}