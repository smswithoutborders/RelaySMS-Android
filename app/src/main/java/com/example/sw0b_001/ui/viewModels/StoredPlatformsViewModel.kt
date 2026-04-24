package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Base64
import android.widget.Toast
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
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.Network
import com.example.sw0b_001.data.grpc.PublisherGrpcImpl
import com.example.sw0b_001.data.grpc.VaultsGrpcImpl
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.ui.views.BottomTabsItems
import com.example.sw0b_001.ui.views.compose.GatewayClientRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.StatusRuntimeException
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@HiltViewModel
class StoredPlatformsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var storedLiveData: LiveData<List<StoredPlatformsEntity>> = MutableLiveData()

    var bottomTabsItem by mutableStateOf(BottomTabsItems.BottomBarRecentTab)

    // Selection mode properties
    var isSelectionMode by mutableStateOf(false)
    var selectedMessagesCount by mutableIntStateOf(0)
    var onSelectAll: (() -> Unit)? = null
    var onDeleteSelected: (() -> Unit)? = null
    var onCancelSelection: (() -> Unit)? = null

    private val db = Datastore.getDatastore(context)?.storedPlatformsDao()
        ?: throw Exception("Cannot open database")

    fun getAccounts(name: String): LiveData<List<StoredPlatformsEntity>> {
        return db.fetchPlatform(name)
    }

    fun getSaved(): LiveData<List<StoredPlatformsEntity>> {
        if(storedLiveData.value.isNullOrEmpty()) {
            storedLiveData = db.fetchAll()
        }
        return storedLiveData
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

        fun verifyPhoneNumberFormat(phoneNumber: String): Boolean {
            val newPhoneNumber = phoneNumber
                .replace("[\\s-]".toRegex(), "")
            return newPhoneNumber.matches("^\\+[1-9]\\d{1,14}$".toRegex())
        }

        fun getPhoneNumberFromUri(context: Context, uri: Uri): String {
            var phoneNumber: String? = null
            val projection: Array<String> = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

            try {
                val cursor: Cursor? = context.contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val numberIndex = it.getColumnIndex(ContactsContract.Contacts.CONTENT_URI.toString())
                        if (numberIndex >= 0) {
                            phoneNumber = it.getString(numberIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }

            return phoneNumber ?: ""
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

        fun triggerAddPlatformRequest(
            context: Context,
            platform: SupportedPlatforms,
            onCompletedCallback: () -> Unit
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                val db = Datastore.getDatastore(context)?.keysDao()
                    ?: throw Exception("Could not open database")

                val publisherPublicKey = db
                    .fetchPublicKey(VaultsGrpcImpl.clientVaultHandshakeKeystoreAliasStaticKeys)
                    ?: throw Exception("Missing private key in credentials for signing")
                when(platform.protocol_type) {
                    Platforms.ProtocolTypes.oauth2.name -> {
                        val publisherGrpcImpl = PublisherGrpcImpl(context)
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
                                response.codeVerifier
                            )

                            val intentUri = response.authorizationUrl.toUri()
                            val intent = oAuth2IntentBuilder(context)
                            intent.launchUrl(context, intentUri)
                        } catch(e: StatusRuntimeException) {
                            e.printStackTrace()
                            CoroutineScope(Dispatchers.Main).launch {
                                e.status.description?.let {
                                    Toast.makeText(context, e.status.description,
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch(e: Exception) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } finally {
                            publisherGrpcImpl.shutdown()
                            onCompletedCallback()
                        }
                    }
                }
            }

        }

        private fun oAuth2IntentBuilder(context: Context): CustomTabsIntent {
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
}