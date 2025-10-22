package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.data.ImageTransmissionProtocol
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Composers
import com.example.sw0b_001.data.PayloadEncryptionComposeDecomposeInit
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.GatewayClientsCommunications
import com.example.sw0b_001.data.Helpers.toBytes
import com.example.sw0b_001.data.Network
import com.example.sw0b_001.data.Publishers
import com.example.sw0b_001.data.SMSHandler
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.Bridges
import com.example.sw0b_001.data.models.Bridges.getKeypairForTransmission
import com.example.sw0b_001.data.models.EncryptedContent
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.extensions.context.settingsGetDefaultGatewayClients
import com.example.sw0b_001.ui.views.BottomTabsItems
import com.example.sw0b_001.ui.views.compose.GatewayClientRequest
import com.example.sw0b_001.ui.views.compose.ReliabilityTestRequestPayload
import com.example.sw0b_001.ui.views.compose.ReliabilityTestResponsePayload
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.Locale

class PlatformsViewModel : ViewModel() {

    private var availableLiveData: LiveData<List<AvailablePlatforms>> = MutableLiveData()
    private var storedLiveData: LiveData<List<StoredPlatformsEntity>> = MutableLiveData()

    var platform by mutableStateOf<AvailablePlatforms?>(null)
    var bottomTabsItem by mutableStateOf(BottomTabsItems.BottomBarRecentTab)

    // Selection mode properties
    var isSelectionMode by mutableStateOf(false)
    var selectedMessagesCount by mutableIntStateOf(0)
    var onSelectAll: (() -> Unit)? = null
    var onDeleteSelected: (() -> Unit)? = null
    var onCancelSelection: (() -> Unit)? = null

    fun getAccounts(context: Context, name: String): LiveData<List<StoredPlatformsEntity>> {
        return Datastore.getDatastore(context).storedPlatformsDao().fetchPlatform(name)
    }

    fun getSaved(context: Context): LiveData<List<StoredPlatformsEntity>> {
        if(storedLiveData.value.isNullOrEmpty()) {
            storedLiveData = Datastore.getDatastore(context).storedPlatformsDao().fetchAll()
        }
        return storedLiveData
    }

    fun getAvailablePlatforms(context: Context): LiveData<List<AvailablePlatforms>> {
        if(availableLiveData.value.isNullOrEmpty()) {
            availableLiveData = Datastore.getDatastore(context).availablePlatformsDao().fetchAll()
        }
        return availableLiveData
    }

    fun getAvailablePlatforms(context: Context, name: String): AvailablePlatforms? {
        return Datastore.getDatastore(context).availablePlatformsDao().fetch(name)
    }

    fun sendPublishingForImage(
        context: Context,
        imageByteArray: ByteArray,
        account: StoredPlatformsEntity? = null,
        text: ByteArray,
        isBridge: Boolean,
        isLoggedIn: Boolean,
        languageCode: String = "en",
        onFailure: (String?) -> Unit,
        onSuccess: (ByteArray?) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            lateinit var payload: ByteArray
            val subscriptionId = context.getDefaultSimSubscription()!!
            try {
                if(isBridge) {
                    if(!isLoggedIn) getKeypairForTransmission(context)
                    val content = Bridges.encryptContent(
                        context,
                        imageByteArray + text,
                        false,
                        imageLength = imageByteArray.size,
                        textLength = text.size,
                        subscriptionId = subscriptionId,
                        isLoggedIn = isLoggedIn
                    )

                    payload = if(isLoggedIn) { Bridges.payloadOnly(content) }
                    else {
                        Bridges.authRequestAndPayload(context, content)
                    }
                }
                else {
                    val platform = Datastore.getDatastore(context).availablePlatformsDao()
                        .fetch(account!!.name!!)
                        ?: return@launch onFailure(
                            context.getString(
                                R.string.could_not_find_platform_details_for,
                                account.name
                            ))

                    val ad = Publishers.fetchPublisherPublicKey(context)
                    payload = PayloadEncryptionComposeDecomposeInit.compose(
                        context = context,
                        content = imageByteArray + text,
                        ad = ad!!,
                        platform = platform,
                        account = account,
                        languageCode = languageCode,
                        subscriptionId = subscriptionId,
                    )
                }

                val gatewayClients = context.settingsGetDefaultGatewayClients
                if(gatewayClients == null) {
                    throw Exception("No default Gateway client")
                }

                ImageTransmissionProtocol.startWorkManager(
                    context = context,
                    formattedPayload = Base64.encode(payload, Base64.DEFAULT),
                    logo = R.drawable.logo,
                    version = ITP_VERSION_VALUE,
                    sessionId = ImageTransmissionProtocol.getItpSession(context).toByte(),
                    imageLength = imageByteArray.size.toShort(),
                    textLength = text.size.toShort(),
                    address = gatewayClients.msisdn,
                    subscriptionId = subscriptionId,
                )
                onSuccess(payload)
            } catch(e: Exception) {
                e.printStackTrace()
                onFailure(e.message)
            }

        }
    }

    fun sendPublishingForMessaging(
        context: Context,
        messageContent: Composers.MessageComposeHandler.MessageContent,
        account: StoredPlatformsEntity,
        subscriptionId: Long,
        smsTransmission: Boolean = true,
        onFailure: (String?) -> Unit,
        onSuccess: (ByteArray?) -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val contentFormatV2Bytes = Composers.MessageComposeHandler
                        .createMessageByteBuffer(
                            from = messageContent.from.value!!,
                            to = messageContent.to.value,
                            message = messageContent.message.value,
                        )

                    val languageCode = Locale.getDefault().language.take(2).lowercase()
                    val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

                    val ad = Publishers.fetchPublisherPublicKey(context)
                        ?: return@withContext onFailure(
                            context.getString(R.string.could_not_fetch_publisher_key))

                    val platform = Datastore.getDatastore(context).availablePlatformsDao()
                        .fetch(account.name!!)
                        ?: return@withContext onFailure(
                            context.getString(
                                R.string.could_not_find_platform_details_for,
                                account.name
                            ))

                    val payload = PayloadEncryptionComposeDecomposeInit.compose(
                        context = context,
                        content = contentFormatV2Bytes,
                        ad = ad,
                        platform = platform,
                        account = account,
                        languageCode = validLanguageCode,
                        subscriptionId = subscriptionId,
                        smsTransmission = smsTransmission,
                    ) {}
                    onSuccess(payload)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onFailure(e.message)
                }
            }
        }
    }

    fun sendPublishingForEmail(
        context: Context,
        emailContent: Composers.EmailComposeHandler.EmailContent,
        account: StoredPlatformsEntity?,
        isBridge: Boolean,
        subscriptionId: Long,
        smsTransmission: Boolean = true,
        onFailureCallback: (String?) -> Unit,
        onCompleteCallback: (ByteArray?) -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if(isBridge) { // if its a bridge message
                        val txtTransmission = Bridges.compose(
                            context = context,
                            to = emailContent.to.value,
                            cc = emailContent.cc.value,
                            bcc = emailContent.bcc.value,
                            subject = emailContent.subject.value,
                            body = emailContent.body.value,
                            imageLength = 0,
                            textLength = 0,
                            smsTransmission = smsTransmission,
                            subscriptionId = subscriptionId
                        )

                        val gatewayClient = context.settingsGetDefaultGatewayClients
                        if(gatewayClient == null) {
                            onFailureCallback("No default Gateway Client...")
                            return@withContext
                        }

                        if(!smsTransmission) {
                            onCompleteCallback(Base64
                                .decode(txtTransmission, Base64.DEFAULT))
                        } else {
                            if(context.isDefault()) {
                                val smsManager = SmsManager(ConversationsViewModel())
                                smsManager.sendSms(
                                    context = context,
                                    text = txtTransmission!!,
                                    address = gatewayClient.msisdn,
                                    subscriptionId = subscriptionId,
                                    threadId = context.getThreadId(gatewayClient.msisdn),
                                    callback = { conversation ->
                                        onCompleteCallback(
                                            Base64.decode(txtTransmission, Base64.DEFAULT))
                                    }
                                )
                            }
                            else {
                                val intent = SMSHandler.transferToDefaultSMSApp(
                                    context,
                                    gatewayClient.msisdn,
                                    txtTransmission
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                    else {
                        if (account == null) {
                            onFailureCallback(context.getString(R.string.account_is_required_for_v1_platform_messages))
                            return@withContext
                        }

                        val ad = Publishers.fetchPublisherPublicKey(context)
                        if (ad == null) {
                            onFailureCallback(context.getString(R.string.could_not_fetch_publisher_key_cannot_encrypt_message))
                            return@withContext
                        }

                        val contentFormatBytes = Composers.EmailComposeHandler.createEmailByteBuffer(
                            from= account.account!!, // 'from' is from the selected account
                            to = emailContent.to.value,
                            cc = emailContent.cc.value,
                            bcc = emailContent.bcc.value,
                            subject = emailContent.subject.value,
                            body = emailContent.body.value,
                            accessToken = if(account.accessToken?.isNotEmpty() == true)
                                account.accessToken else null,
                            refreshToken =if(account.refreshToken?.isNotEmpty() == true)
                                account.refreshToken else null,
                            isBridge = false
                        )

                        val platform = Datastore.getDatastore(context).availablePlatformsDao()
                            .fetch(account.name!!)

                        if (platform == null) {
                            onFailureCallback(
                                context.getString(R.string.could_not_fetch_publisher_key))
                            return@withContext
                        }

                        val languageCode = Locale.getDefault().language.take(2).lowercase(Locale.ROOT)
                        val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

                        val payload = PayloadEncryptionComposeDecomposeInit.compose(
                            context = context,
                            content = contentFormatBytes,
                            ad = ad,
                            platform = platform,
                            account = account,
                            languageCode = validLanguageCode,
                            smsTransmission = smsTransmission,
                            subscriptionId = subscriptionId,
                        ) { }
                        onCompleteCallback(payload)
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    onFailureCallback(e.message)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, e.message ?: "An unknown error occurred", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun sendPublishingForTest(
        context: Context,
        startTime: String,
        platform: AvailablePlatforms,
        subscriptionId: Long,
        onFailure: (String?) -> Unit,
        onSuccess: () -> Unit,
        smsTransmission: Boolean = true
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val gatewayClient = context.settingsGetDefaultGatewayClients
                        ?: return@withContext onFailure("No Gateway Client set for testing.")
                    val url = context.getString(R.string.test_url,
                        gatewayClient.msisdn)
                    val requestPayload = Json.encodeToString(ReliabilityTestRequestPayload(startTime))
                    val response = Network.jsonRequestPost(url, requestPayload)
                    val responsePayload = Json.decodeFromString<ReliabilityTestResponsePayload>(response.result.get())
                    val testId = responsePayload.test_id.toString()
                    val AD = Publishers.fetchPublisherPublicKey(context)
                        ?: return@withContext onFailure("Could not fetch publisher key.")

                    val contentFormatV2Bytes = createTestByteBuffer(testId).array()

                    val languageCode = Locale.getDefault().language.take(2).lowercase()
                    val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

                    val v2PayloadBytes = PayloadEncryptionComposeDecomposeInit.compose(
                        context = context,
                        content = contentFormatV2Bytes,
                        ad = AD,
                        platform = platform,
                        account = null,
                        languageCode = validLanguageCode,
                        smsTransmission = true,
                        subscriptionId = subscriptionId
                    )

                    if (smsTransmission) {
                        val base64Payload = Base64.encodeToString(v2PayloadBytes, Base64.NO_WRAP)
                        SMSHandler.transferToDefaultSMSApp(
                            context,
                            gatewayClient.msisdn,
                            base64Payload
                        )
                    }
                    onSuccess()

                } catch (e: Exception) {
                    e.printStackTrace()
                    onFailure(e.message)
                }
            }

        }
    }

    private fun createTestByteBuffer(testId: String): ByteBuffer {
        val BYTE_SIZE_LIMIT = 255
        val testIdBytes = testId.toByteArray(StandardCharsets.UTF_8)
        val testIdSize = testIdBytes.size

        if (testIdSize > BYTE_SIZE_LIMIT) throw IllegalArgumentException("Test ID exceeds maximum size of $BYTE_SIZE_LIMIT bytes")

        val totalSize = 1 + 2 + 2 + 2 + 1 + 2 + 2 + 2 + testIdSize
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put(testIdSize.toByte()) // 1 byte for from field length (test ID)
        buffer.putShort(0)              // 2 bytes for to length
        buffer.putShort(0)              // 2 bytes for cc length
        buffer.putShort(0)              // 2 bytes for bcc length
        buffer.put(0.toByte())          // 1 byte for subject length
        buffer.putShort(0)              // 2 bytes for body length
        buffer.putShort(0)              // 2 bytes for access token length
        buffer.putShort(0)              // 2 bytes for refresh token length

        buffer.put(testIdBytes)
        buffer.flip()
        return buffer
    }

    fun sendPublishingForPost(
        context: Context,
        text: String,
        account: StoredPlatformsEntity,
        subscriptionId: Long,
        onFailure: (String?) -> Unit,
        onSuccess: (ByteArray?) -> Unit,
        smsTransmission: Boolean = true
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try {
                    val AD = Publishers.fetchPublisherPublicKey(context)
                        ?: return@withContext onFailure("Could not fetch publisher key.")

                    val contentFormatV2Bytes = Composers.TextComposeHandler.createTextByteBuffer(
                        from = account.account!!,
                        body = text,
                        accessToken = account.accessToken,
                        refreshToken = account.refreshToken
                    )

                    val platform = Datastore.getDatastore(context).availablePlatformsDao().fetch(account.name!!)
                        ?: return@withContext onFailure("Could not find platform details for '${account.name}'.")

                    val languageCode = Locale.getDefault().language.take(2).lowercase()
                    val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

                    val v2PayloadBytes = PayloadEncryptionComposeDecomposeInit.compose(
                        context = context,
                        content = contentFormatV2Bytes,
                        ad = AD,
                        platform = platform,
                        account = account,
                        languageCode = validLanguageCode,
                        smsTransmission = smsTransmission,
                        subscriptionId = subscriptionId
                    )

                    if (smsTransmission) {
                        val gatewayClient = context.settingsGetDefaultGatewayClients
                            ?: return@withContext onFailure("No Gateway Client set.")
                        val base64Payload = Base64.encodeToString(v2PayloadBytes, Base64.NO_WRAP)
                        SMSHandler.transferToDefaultSMSApp(
                            context,
                            gatewayClient.msisdn,
                            base64Payload
                        )
                    }
                    onSuccess(v2PayloadBytes)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onFailure(e.message)
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
            platform: AvailablePlatforms,
            onCompletedCallback: () -> Unit
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                when(platform.protocol_type) {
                    Platforms.ProtocolTypes.oauth2.name -> {
                        val publishers = Publishers(context)
                        val publicKeyBytes = Publishers.fetchPublisherPublicKey(context)
                        val requestIdentifier = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP)
                        try {
                            val response = publishers.getOAuthURL(
                                availablePlatforms = platform,
                                autogenerateCodeVerifier = true,
                                supportsUrlScheme = platform.support_url_scheme!!,
                                requestIdentifier = requestIdentifier
                            )

                            Publishers.storeOauthRequestCodeVerifier(context, response.codeVerifier)

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
                            publishers.shutdown()
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