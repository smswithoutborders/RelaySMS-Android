package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Base64
import android.util.Log
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
import com.afkanerd.lib_image_android.data.SmsWorkManager
import com.afkanerd.lib_image_android.data.getItpSession
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.example.sw0b_001.MainActivity
import com.example.sw0b_001.R
import com.example.sw0b_001.data.ComposeHandlers
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.GatewayClientsCommunications
import com.example.sw0b_001.data.Helpers.toBytes
import com.example.sw0b_001.data.Network
import com.example.sw0b_001.data.Publishers
import com.example.sw0b_001.data.SMSHandler
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.Bridges
import com.example.sw0b_001.data.models.EncryptedContent
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
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
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.Locale

class PlatformsViewModel : ViewModel() {

    var accountsForMissingDialog by mutableStateOf<Map<String, List<String>>>(emptyMap())

    private var availableLiveData: LiveData<List<AvailablePlatforms>> = MutableLiveData()
    private var storedLiveData: LiveData<List<StoredPlatformsEntity>> = MutableLiveData()

    var platform by mutableStateOf<AvailablePlatforms?>(null)
    var message by mutableStateOf<EncryptedContent?>(null)
    var bottomTabsItem by mutableStateOf(BottomTabsItems.BottomBarRecentTab)

    // Selection mode properties
    var isSelectionMode by mutableStateOf(false)
    var selectedMessagesCount by mutableIntStateOf(0)
    var onSelectAll: (() -> Unit)? = null
    var onDeleteSelected: (() -> Unit)? = null
    var onCancelSelection: (() -> Unit)? = null

    fun reset() {
        platform = null
        message = null
    }

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

    fun getAccount(context: Context, accountIdentifier: String): StoredPlatformsEntity? {
        return Datastore.getDatastore(context).storedPlatformsDao().fetchAccount(accountIdentifier)
    }

    fun sendPublishingForImage(
        context: Context,
        imageViewModel: ImageViewModel,
        account: StoredPlatformsEntity? = null,
        text: String,
        isBridge: Boolean,
        isLoggedIn: Boolean,
        languageCode: String = "en",
        subscriptionId: Long = -1,
        onFailure: (String?) -> Unit,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            lateinit var payload: ByteArray

            try {
                if(isBridge) {
                    val clientPublicKey: ByteArray? = if(isLoggedIn)
                        Publishers.fetchClientPublisherPublicKey(context) else
                        Bridges.getKeypairForTransmission(context)

                    val content = Bridges.encryptContent(
                        context,
                        imageViewModel.processedImage.value!!.rawBytes!! +
                                text.encodeToByteArray(),
                        false,
                    )

                    payload = if(isLoggedIn) { Bridges.payloadOnly(content) }
                    else {
                        Bridges.authRequestAndPayload(clientPublicKey!!,
                            content)
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
                    payload = ComposeHandlers.composeV2(
                        context = context,
                        contentFormatV2Bytes = imageViewModel.processedImage.value!!.rawBytes!! +
                                text.encodeToByteArray(),
                        AD = ad!!,
                        platform = platform,
                        account = account,
                        languageCode = languageCode,
                        subscriptionId = subscriptionId,
                    )
                }

                imageViewModel.startWorkManager(
                    context = context,
                    formattedPayload = payload,
                    logo = R.drawable.logo,
                    version = ITP_VERSION_VALUE,
                    sessionId = context.getItpSession().toByte(),
                    imageLength = imageViewModel.processedImage.value!!.rawBytes!!.size.toBytes(),
                    textLength = text.length.toBytes(),
                )
                onSuccess()
            } catch(e: Exception) {
                e.printStackTrace()
                onFailure(e.message)
            }

        }
    }

    fun sendPublishingForMessaging(
        context: Context,
        messageContent: MessageComposeHandler.MessageContent,
        account: StoredPlatformsEntity,
        subscriptionId: Long,
        smsTransmission: Boolean = true,
        onFailure: (String?) -> Unit,
        onSuccess: (EncryptedContent) -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val contentFormatV2Bytes = createMessageByteBuffer(
                        from = messageContent.from.value!!,
                        to = messageContent.to.value,
                        message = messageContent.message.value!!,
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

                    ComposeHandlers.composeV2(
                        context = context,
                        contentFormatV2Bytes = contentFormatV2Bytes,
                        AD = ad,
                        platform = platform,
                        account = account,
                        languageCode = validLanguageCode,
                        subscriptionId = subscriptionId,
                        smsTransmission = smsTransmission,
                        onSuccessRunnable = onSuccess,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    onFailure(e.message)
                }
            }
        }
    }

    private fun createMessageByteBuffer(
        from: String,
        to: String,
        message: String
    ): ByteArray {

        // Convert strings to byte arrays
        val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
        val toBytes = to.toByteArray(StandardCharsets.UTF_8)
        val bodyBytes = message.toByteArray(StandardCharsets.UTF_8)

        val buffer = ByteBuffer.allocate(14 +
                fromBytes.size + toBytes.size + bodyBytes.size).order(ByteOrder.LITTLE_ENDIAN)

        // Write field lengths according to specification
        buffer.put(fromBytes.size.toByte())
        buffer.putShort(toBytes.size.toShort())
        buffer.putShort(0)
        buffer.putShort(0)
        buffer.put(0.toByte())
        buffer.putShort(bodyBytes.size.toShort())
        buffer.putShort(0.toShort()) // access token
        buffer.putShort(0.toShort()) // refresh token

        // Write field values
        buffer.put(fromBytes)
        buffer.put(toBytes)
        buffer.put(bodyBytes)

        return buffer.array()
    }

    fun sendPublishingForEmail(
        context: Context,
        emailContent: EmailComposeHandler.EmailContent,
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
                        ) { onCompleteCallback(null) }.first

                        val gatewayClientMSISDN = GatewayClientsCommunications(context)
                            .getDefaultGatewayClient()

                        gatewayClientMSISDN?.let {
                            if(context.isDefault()) {
                                val smsManager = SmsManager(ConversationsViewModel())
                                smsManager.sendSms(
                                    context = context,
                                    text = txtTransmission!!,
                                    address = gatewayClientMSISDN,
                                    subscriptionId = subscriptionId,
                                    threadId = context.getThreadId(gatewayClientMSISDN),
                                    callback = { conversation -> onCompleteCallback(null) }
                                )
                            }
                            else {
                                val intent = SMSHandler.transferToDefaultSMSApp(
                                    context,
                                    gatewayClientMSISDN,
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

                        val contentFormatBytes = if (
                            account.accessToken?.isNotEmpty() == true
                        ) {
                            createEmailByteBuffer(
                                from = account.account!!, // 'from' is from the selected account
                                to = emailContent.to.value,
                                cc = emailContent.cc.value,
                                bcc = emailContent.bcc.value,
                                subject = emailContent.subject.value,
                                body = emailContent.body.value,
                                accessToken = account.accessToken,
                                refreshToken = account.refreshToken
                            )
                        } else {
                            createEmailByteBuffer(
                                from = account.account!!,
                                to = emailContent.to.value,
                                cc = emailContent.cc.value,
                                bcc = emailContent.bcc.value,
                                subject = emailContent.subject.value,
                                body = emailContent.body.value,
                            )
                        }

                        val platform = Datastore.getDatastore(context).availablePlatformsDao()
                            .fetch(account.name!!)

                        if (platform == null) {
                            onFailureCallback(
                                context.getString(R.string.could_not_fetch_publisher_key))
                            return@withContext
                        }

                        val languageCode = Locale.getDefault().language.take(2).lowercase(Locale.ROOT)
                        val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

                        val payload = ComposeHandlers.composeV2(
                            context = context,
                            contentFormatV2Bytes = contentFormatBytes,
                            AD = ad,
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

    private fun createEmailByteBuffer(
        from: String, to: String, cc: String, bcc: String, subject: String, body: String,
        accessToken: String? = null, refreshToken: String? = null
    ): ByteArray {
        val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
        val toBytes = to.toByteArray(StandardCharsets.UTF_8)
        val ccBytes = cc.toByteArray(StandardCharsets.UTF_8)
        val bccBytes = bcc.toByteArray(StandardCharsets.UTF_8)
        val subjectBytes = subject.toByteArray(StandardCharsets.UTF_8)
        val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
        val accessTokenBytes = accessToken?.toByteArray(StandardCharsets.UTF_8)
        val refreshTokenBytes = refreshToken?.toByteArray(StandardCharsets.UTF_8)

        // Calculate total size for the buffer
        val totalSize = 1 + 2 + 2 + 2 + 1 + 2 + 2 + 2 +
                fromBytes.size + toBytes.size + ccBytes.size + bccBytes.size +
                subjectBytes.size + bodyBytes.size +
                (accessTokenBytes?.size ?: 0) + (refreshTokenBytes?.size ?: 0)

        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        // Write field lengths
        buffer.put(fromBytes.size.toByte())
        buffer.putShort(toBytes.size.toShort())
        buffer.putShort(ccBytes.size.toShort())
        buffer.putShort(bccBytes.size.toShort())
        buffer.put(subjectBytes.size.toByte())
        buffer.putShort(bodyBytes.size.toShort())
        buffer.putShort((accessTokenBytes?.size ?: 0).toShort())
        buffer.putShort((refreshTokenBytes?.size ?: 0).toShort())

        // Write field values
        buffer.put(fromBytes)
        buffer.put(toBytes)
        buffer.put(ccBytes)
        buffer.put(bccBytes)
        buffer.put(subjectBytes)
        buffer.put(bodyBytes)
        accessTokenBytes?.let { buffer.put(it) }
        refreshTokenBytes?.let { buffer.put(it) }

        return buffer.array()
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
                    val gatewayClientMSISDN = GatewayClientsCommunications(context).getDefaultGatewayClient()
                        ?: return@withContext onFailure("No Gateway Client set for testing.")
                    val url = context.getString(R.string.test_url, gatewayClientMSISDN)
                    val requestPayload = Json.encodeToString(ReliabilityTestRequestPayload(startTime))
                    val response = Network.jsonRequestPost(url, requestPayload)
                    val responsePayload = Json.decodeFromString<ReliabilityTestResponsePayload>(response.result.get())
                    val testId = responsePayload.test_id.toString()
                    val AD = Publishers.fetchPublisherPublicKey(context)
                        ?: return@withContext onFailure("Could not fetch publisher key.")

                    val contentFormatV2Bytes = createTestByteBuffer(testId).array()

                    val languageCode = Locale.getDefault().language.take(2).lowercase()
                    val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

                    val v2PayloadBytes = ComposeHandlers.composeV2(
                        context = context,
                        contentFormatV2Bytes = contentFormatV2Bytes,
                        AD = AD,
                        platform = platform,
                        account = null,
                        languageCode = validLanguageCode,
                        smsTransmission = true,
                        subscriptionId = subscriptionId
                    ){}

                    if (smsTransmission) {
                        val base64Payload = Base64.encodeToString(v2PayloadBytes, Base64.NO_WRAP)
                        SMSHandler.transferToDefaultSMSApp(context, gatewayClientMSISDN, base64Payload)
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
        onSuccess: () -> Unit,
        smsTransmission: Boolean = true
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try {
                    val AD = Publishers.fetchPublisherPublicKey(context)
                        ?: return@withContext onFailure("Could not fetch publisher key.")

                    val contentFormatV2Bytes = createTextByteBuffer(
                        from = account.account!!,
                        body = text,
                        accessToken = account.accessToken,
                        refreshToken = account.refreshToken
                    ).array()

                    val platform = Datastore.getDatastore(context).availablePlatformsDao().fetch(account.name!!)
                        ?: return@withContext onFailure("Could not find platform details for '${account.name}'.")

                    val languageCode = Locale.getDefault().language.take(2).lowercase()
                    val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

                    val v2PayloadBytes = ComposeHandlers.composeV2(
                        context = context,
                        contentFormatV2Bytes = contentFormatV2Bytes,
                        AD = AD,
                        platform = platform,
                        account = account,
                        languageCode = validLanguageCode,
                        smsTransmission = smsTransmission,
                        subscriptionId = subscriptionId
                    )

                    if (smsTransmission) {
                        val gatewayClientMSISDN = GatewayClientsCommunications(context).getDefaultGatewayClient()
                            ?: return@withContext onFailure("No default gateway client configured.")
                        val base64Payload = Base64.encodeToString(v2PayloadBytes, Base64.NO_WRAP)
                        SMSHandler.transferToDefaultSMSApp(context, gatewayClientMSISDN, base64Payload)
                    }
                    onSuccess()
                } catch (e: Exception) {
                    e.printStackTrace()
                    onFailure(e.message)
                }
            }

        }
    }

    private fun createTextByteBuffer(
        from: String, body: String,
        accessToken: String? = null, refreshToken: String? = null
    ): ByteBuffer {
        // Define size constants
        val BYTE_SIZE_LIMIT = 255
        val SHORT_SIZE_LIMIT = 65535

        // Convert strings to byte arrays
        val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
        val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
        val accessTokenBytes = accessToken?.toByteArray(StandardCharsets.UTF_8)
        val refreshTokenBytes = refreshToken?.toByteArray(StandardCharsets.UTF_8)

        // Get sizes for validation and buffer allocation
        val fromSize = fromBytes.size
        val bodySize = bodyBytes.size
        val accessTokenSize = accessTokenBytes?.size ?: 0
        val refreshTokenSize = refreshTokenBytes?.size ?: 0

        // Validate field sizes
        if (fromSize > BYTE_SIZE_LIMIT) throw IllegalArgumentException("From field exceeds maximum size of $BYTE_SIZE_LIMIT bytes")
        if (bodySize > SHORT_SIZE_LIMIT) throw IllegalArgumentException("Body field exceeds maximum size of $SHORT_SIZE_LIMIT bytes")
        if (accessTokenSize > SHORT_SIZE_LIMIT) throw IllegalArgumentException("Access token exceeds maximum size of $SHORT_SIZE_LIMIT bytes")
        if (refreshTokenSize > SHORT_SIZE_LIMIT) throw IllegalArgumentException("Refresh token exceeds maximum size of $SHORT_SIZE_LIMIT bytes")

        val totalSize = 1 + 2 + 2 + 2 + 1 + 2 + 2 + 2 +
                fromSize + bodySize + accessTokenSize + refreshTokenSize

        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        // Write field lengths
        buffer.put(fromSize.toByte())
        buffer.putShort(0)
        buffer.putShort(0)
        buffer.putShort(0)
        buffer.put(0.toByte())
        buffer.putShort(bodySize.toShort())
        buffer.putShort(accessTokenSize.toShort())
        buffer.putShort(refreshTokenSize.toShort())

        // Write field values
        buffer.put(fromBytes)
        buffer.put(bodyBytes)
        accessTokenBytes?.let { buffer.put(it) }
        refreshTokenBytes?.let { buffer.put(it) }

        buffer.flip()
        return buffer
    }

    object EmailComposeHandler {
        @Serializable
        data class EmailContent(
            @Serializable(with = MutableStateSerializer::class)
            var to: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            var cc: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            var bcc: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            var subject: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            var body: MutableState<String> = mutableStateOf("")
        )

        fun decomposeMessage(contentBytes: ByteArray): EmailContent {
            try {
                val buffer = ByteBuffer.wrap(contentBytes).order(ByteOrder.LITTLE_ENDIAN)

                val fromLen = buffer.get().toInt() and 0xFF
                val toLen = buffer.getShort().toInt() and 0xFFFF
                val ccLen = buffer.getShort().toInt() and 0xFFFF
                val bccLen = buffer.getShort().toInt() and 0xFFFF
                val subjectLen = buffer.get().toInt() and 0xFF
                val bodyLen = buffer.getShort().toInt() and 0xFFFF
                val accessLen = buffer.getShort().toInt() and 0xFFFF
                val refreshLen = buffer.getShort().toInt() and 0xFFFF

                // Skip 'from' field
                if (fromLen > 0) buffer.position(buffer.position() + fromLen)

                // Read the relevant fields for the EmailContent object
                val to = ByteArray(toLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
                val cc = ByteArray(ccLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
                val bcc = ByteArray(bccLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
                val subject = ByteArray(subjectLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
                val body = ByteArray(bodyLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

                // Skip token fields
                if (accessLen > 0) buffer.position(buffer.position() + accessLen)
                if (refreshLen > 0) buffer.position(buffer.position() + refreshLen)

                return EmailContent(
                    mutableStateOf(to),
                    mutableStateOf(cc),
                    mutableStateOf(bcc),
                    mutableStateOf(subject),
                    mutableStateOf(body),
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return EmailContent()
            }
        }

        fun decomposeBridgeMessage(message: String): EmailContent {
            return try {
                // Bridge messages typically don't include 'from' in their direct content string
                // Format: to:cc:bcc:subject:body
                val parts = message.split(":", limit = 5)
                if (parts.size < 5) {
                    EmailContent(
                        to = mutableStateOf(parts.getOrElse(0) { "" }),
                        cc = mutableStateOf(parts.getOrElse(1) { "" }),
                        bcc = mutableStateOf(parts.getOrElse(2) { "" }),
                        subject = mutableStateOf(parts.getOrElse(3) { "" }),
                        body = mutableStateOf(parts.getOrElse(4) { "" }),
                    )
                } else {
                    EmailContent(
                        to = mutableStateOf(parts[0]),
                        cc = mutableStateOf(parts[1]),
                        bcc = mutableStateOf(parts[2]),
                        subject = mutableStateOf(parts[3]),
                        body = mutableStateOf(parts[4]),
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                EmailContent()
            }
        }
    }

    object TextComposeHandler {
        @Serializable
        data class TextContent(
            @Serializable(with = MutableStateSerializer::class)
            val from: MutableState<String?> = mutableStateOf(null),
            @Serializable(with = MutableStateSerializer::class)
            val text: MutableState<String> = mutableStateOf(""),
        )

        fun decomposeMessage(contentBytes: ByteArray): TextContent {
            return try {
                val buffer = ByteBuffer.wrap(contentBytes).order(ByteOrder.LITTLE_ENDIAN)

                val fromLen = buffer.get().toInt() and 0xFF
                val toLen = buffer.getShort().toInt() and 0xFFFF
                val ccLen = buffer.getShort().toInt() and 0xFFFF
                val bccLen = buffer.getShort().toInt() and 0xFFFF
                val subjectLen = buffer.get().toInt() and 0xFF
                val bodyLen = buffer.getShort().toInt() and 0xFFFF
                val accessLen = buffer.getShort().toInt() and 0xFFFF
                val refreshLen = buffer.getShort().toInt() and 0xFFFF

                val from = ByteArray(fromLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

                // Skip unused fields
                if (toLen > 0) buffer.position(buffer.position() + toLen)
                if (ccLen > 0) buffer.position(buffer.position() + ccLen)
                if (bccLen > 0) buffer.position(buffer.position() + bccLen)
                if (subjectLen > 0) buffer.position(buffer.position() + subjectLen)

                val text = ByteArray(bodyLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

                // Skip token fields
                if (accessLen > 0) buffer.position(buffer.position() + accessLen)
                if (refreshLen > 0) buffer.position(buffer.position() + refreshLen)

                TextContent(
                    from = mutableStateOf(from),
                    text = mutableStateOf(text)
                )
            } catch (e: Exception) {
                TextContent()
            }
        }
    }

    object MessageComposeHandler {
        @Serializable
        data class MessageContent(
            @Serializable(with = MutableStateSerializer::class)
            val from: MutableState<String?> = mutableStateOf(null),
            @Serializable(with = MutableStateSerializer::class)
            val to: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            val message: MutableState<String> = mutableStateOf(""),
        )

        fun decomposeMessage(contentBytes: ByteArray): MessageContent {
            return try {
                val buffer = ByteBuffer.wrap(contentBytes).order(ByteOrder.LITTLE_ENDIAN)

                val fromLen = buffer.get().toInt() and 0xFF
                val toLen = buffer.getShort().toInt() and 0xFFFF
                val ccLen = buffer.getShort().toInt() and 0xFFFF
                val bccLen = buffer.getShort().toInt() and 0xFFFF
                val subjectLen = buffer.get().toInt() and 0xFF
                val bodyLen = buffer.getShort().toInt() and 0xFFFF
                val accessLen = buffer.getShort().toInt() and 0xFFFF
                val refreshLen = buffer.getShort().toInt() and 0xFFFF

                val from = ByteArray(fromLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
                val to = ByteArray(toLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

                if (ccLen > 0) buffer.position(buffer.position() + ccLen)
                if (bccLen > 0) buffer.position(buffer.position() + bccLen)
                if (subjectLen > 0) buffer.position(buffer.position() + subjectLen)

                val message = ByteArray(bodyLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

                if (accessLen > 0) buffer.position(buffer.position() + accessLen)
                if (refreshLen > 0) buffer.position(buffer.position() + refreshLen)

                MessageContent(
                    from = mutableStateOf(from),
                    to = mutableStateOf(to),
                    message = mutableStateOf(message)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                MessageContent()
            }
        }
    }

    companion object {
        const val ITP_VERSION_VALUE: Byte = 0x04

        fun verifyPhoneNumberFormat(phoneNumber: String): Boolean {
            val newPhoneNumber = phoneNumber
                .replace("[\\s-]".toRegex(), "")
            return newPhoneNumber.matches("^\\+[1-9]\\d{1,14}$".toRegex())
        }

        fun getPhoneNumberFromUri(context: Context, uri: Uri): String {
            var phoneNumber: String? = null
            val projection: Array<String> = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

            try {
                val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
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
                    Platforms.ProtocolTypes.OAUTH2.type -> {
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