package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.data.Composers
import com.example.sw0b_001.data.models.Accounts
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class PublisherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    companion object {
        init {
            System.loadLibrary("librelaysms_spec_payload")
        }
    }

    fun sendPublishingForImage(
        imageByteArray: ByteArray,
        account: Accounts? = null,
        text: ByteArray,
        isBridge: Boolean,
        isLoggedIn: Boolean,
        languageCode: String = "en",
        onFailure: (String?) -> Unit,
        onSuccess: (ByteArray?) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
//            lateinit var payload: ByteArray
//            val subscriptionId = context.getDefaultSimSubscription()!!
//            try {
//                if(isBridge) {
//                    TODO()
////                    val random = (0..255).random()
////                    val content = Bridges.encryptContent(
////                        context,
////                        imageByteArray + text,
////                        false,
////                        imageLength = imageByteArray.size,
////                        textLength = text.size,
////                        subscriptionId = subscriptionId,
////                        isLoggedIn = isLoggedIn
////                    )
////
////                    payload = if(isLoggedIn) { Bridges.payloadOnly(content) }
////                    else {
////                        Bridges.authRequestAndPayload(
////                            content = content,
////                            serverKID = random.toUByte(),
////                            clientPublicKey = TODO()
////                        )
////                    }
//                }
//                else {
//                    TODO()
////                    val platform = Datastore.getDatastore(context).availablePlatformsDao()
////                        .fetch(account!!.name!!)
////                        ?: return@launch onFailure(
////                            context.getString(
////                                R.string.could_not_find_platform_details_for,
////                                account.name
////                            ))
////
////                    val ad = TODO()
////                    payload = PublishersImpl.compose(
////                        context = context,
////                        content = imageByteArray + text,
////                        ad = ad!!,
////                        platform = platform,
////                        account = account,
////                        languageCode = languageCode,
////                        subscriptionId = subscriptionId,
////                    )
//                }
//
////                val gatewayClients = context.settingsGetDefaultGatewayClients
////                    ?: throw Exception("No default Gateway client")
////
////                ImageTransmissionProtocol.startWorkManager(
////                    context = context,
////                    formattedPayload = Base64.encode(payload, Base64.DEFAULT),
////                    logo = R.drawable.logo,
////                    version = ITP_VERSION_VALUE,
////                    sessionId = ImageTransmissionProtocol.getItpSession(context).toByte(),
////                    imageLength = imageByteArray.size.toShort(),
////                    textLength = text.size.toShort(),
////                    address = gatewayClients.msisdn,
////                    subscriptionId = subscriptionId,
////                )
////                onSuccess(payload)
////            } catch(e: Exception) {
////                e.printStackTrace()
////                onFailure(e.message)
////            }
//
//        }
        }
    }

    fun sendPublishingForMessaging(
        messageContent: Composers.MessageComposeHandler.MessageContent,
        account: Accounts,
        subscriptionId: Long,
        smsTransmission: Boolean = true,
        onFailure: (String?) -> Unit,
        onSuccess: (ByteArray?) -> Unit,
    ) {
        TODO()
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                try {
//                    val contentFormatV2Bytes = Composers.MessageComposeHandler
//                        .createMessageByteBuffer(
//                            from = messageContent.from.value!!,
//                            to = messageContent.to.value,
//                            message = messageContent.message.value,
//                        )
//
//                    val languageCode = Locale.getDefault().language.take(2).lowercase()
//                    val validLanguageCode = if (languageCode.length == 2) languageCode else "en"
//
//                    val ad = TODO()
//
//                    val platform = Datastore.getDatastore(context).availablePlatformsDao()
//                        .fetch(account.name!!)
//                        ?: return@withContext onFailure(
//                            context.getString(
//                                R.string.could_not_find_platform_details_for,
//                                account.name
//                            ))
//
//                    val payload = PublishersImpl.compose(
//                        context = context,
//                        content = contentFormatV2Bytes,
//                        ad = ad,
//                        platform = platform,
//                        account = account,
//                        languageCode = validLanguageCode,
//                        subscriptionId = subscriptionId,
//                        smsTransmission = smsTransmission,
//                    ) {}
//                    onSuccess(payload)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    onFailure(e.message)
//                }
//            }
//        }
    }

    fun sendPublishingForEmail(
        emailContent: Composers.EmailComposeHandler.EmailContent,
        account: Accounts?,
        isBridge: Boolean,
        subscriptionId: Long,
        smsTransmission: Boolean = true,
        onFailureCallback: (String?) -> Unit,
        onCompleteCallback: (ByteArray?) -> Unit,
    ) {
        TODO()
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                try {
//                    if(isBridge) { // if its a bridge message
//                        val txtTransmission = Bridges.compose(
//                            context = context,
//                            to = emailContent.to.value,
//                            cc = emailContent.cc.value,
//                            bcc = emailContent.bcc.value,
//                            subject = emailContent.subject.value,
//                            body = emailContent.body.value,
//                            imageLength = 0,
//                            textLength = 0,
//                            smsTransmission = smsTransmission,
//                            subscriptionId = subscriptionId
//                        )
//
//                        val gatewayClient = context.settingsGetDefaultGatewayClients
//                        if(gatewayClient == null) {
//                            onFailureCallback("No default Gateway Client...")
//                            return@withContext
//                        }
//
//                        if(!smsTransmission) {
//                            onCompleteCallback(Base64
//                                .decode(txtTransmission, Base64.DEFAULT))
//                        } else {
//                            if(context.isDefault()) {
//                                val smsManager = SmsManager(ConversationsViewModel())
//                                smsManager.sendSms(
//                                    context = context,
//                                    text = txtTransmission!!,
//                                    address = gatewayClient.msisdn,
//                                    subscriptionId = subscriptionId,
//                                    threadId = context.getThreadId(gatewayClient.msisdn),
//                                    callback = { conversation ->
//                                        onCompleteCallback(
//                                            Base64.decode(txtTransmission, Base64.DEFAULT))
//                                    }
//                                )
//                            }
//                            else {
//                                val intent = SMSHandler.transferToDefaultSMSApp(
//                                    context,
//                                    gatewayClient.msisdn,
//                                    txtTransmission
//                                ).apply {
//                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                                }
//                                context.startActivity(intent)
//                            }
//                            onCompleteCallback(null)
//                        }
//                    }
//                    else {
//                        if (account == null) {
//                            onFailureCallback(context.getString(R.string.account_is_required_for_v1_platform_messages))
//                            return@withContext
//                        }
//
//                        val ad = TODO()
//                        if (ad == null) {
//                            onFailureCallback(context.getString(R.string.could_not_fetch_publisher_key_cannot_encrypt_message))
//                            return@withContext
//                        }
//
//                        val contentFormatBytes = Composers.EmailComposeHandler.createEmailByteBuffer(
//                            from= account.account!!, // 'from' is from the selected account
//                            to = emailContent.to.value,
//                            cc = emailContent.cc.value,
//                            bcc = emailContent.bcc.value,
//                            subject = emailContent.subject.value,
//                            body = emailContent.body.value,
//                            accessToken = if(account.accessToken?.isNotEmpty() == true)
//                                account.accessToken else null,
//                            refreshToken =if(account.refreshToken?.isNotEmpty() == true)
//                                account.refreshToken else null,
//                            isBridge = false
//                        )
//
//                        val platform = Datastore.getDatastore(context).availablePlatformsDao()
//                            .fetch(account.name!!)
//
//                        if (platform == null) {
//                            onFailureCallback(
//                                context.getString(R.string.could_not_fetch_publisher_key))
//                            return@withContext
//                        }
//
//                        val languageCode = Locale.getDefault().language.take(2).lowercase(Locale.ROOT)
//                        val validLanguageCode = if (languageCode.length == 2) languageCode else "en"
//
//                        val payload = PublishersImpl.compose(
//                            context = context,
//                            content = contentFormatBytes,
//                            ad = ad,
//                            platform = platform!!,
//                            account = account,
//                            languageCode = validLanguageCode,
//                            smsTransmission = smsTransmission,
//                            subscriptionId = subscriptionId,
//                        ) { }
//                        onCompleteCallback(payload)
//                    }
//                }
//                catch (e: Exception) {
//                    e.printStackTrace()
//                    onFailureCallback(e.message)
//                    CoroutineScope(Dispatchers.Main).launch {
//                        Toast.makeText(context, e.message ?: "An unknown error occurred", Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        }
    }

    fun sendPublishingForPost(
        text: String,
        account: Accounts,
        subscriptionId: Long,
        onFailure: (String?) -> Unit,
        onSuccess: (ByteArray?) -> Unit,
        smsTransmission: Boolean = true
    ) {
        TODO()
//        viewModelScope.launch {
//            withContext(Dispatchers.IO){
//                try {
//                    val AD = TODO()
//
//                    val contentFormatV2Bytes = Composers.TextComposeHandler.createTextByteBuffer(
//                        from = account.account!!,
//                        body = text,
//                        accessToken = account.accessToken,
//                        refreshToken = account.refreshToken
//                    )
//
//                    val platform = Datastore.getDatastore(context).availablePlatformsDao().fetch(account.name!!)
//                        ?: return@withContext onFailure("Could not find platform details for '${account.name}'.")
//
//                    val languageCode = Locale.getDefault().language.take(2).lowercase()
//                    val validLanguageCode = if (languageCode.length == 2) languageCode else "en"
//
//                    val v2PayloadBytes = PublishersImpl.compose(
//                        context = context,
//                        content = contentFormatV2Bytes,
//                        ad = AD,
//                        platform = platform,
//                        account = account,
//                        languageCode = validLanguageCode,
//                        smsTransmission = smsTransmission,
//                        subscriptionId = subscriptionId
//                    )
//
//                    if (smsTransmission) {
//                        val gatewayClient = context.settingsGetDefaultGatewayClients
//                            ?: return@withContext onFailure("No Gateway Client set.")
//                        val base64Payload = Base64.encodeToString(v2PayloadBytes, Base64.NO_WRAP)
//                        SMSHandler.transferToDefaultSMSApp(
//                            context,
//                            gatewayClient.msisdn,
//                            base64Payload
//                        )
//                    }
//                    onSuccess(v2PayloadBytes)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    onFailure(e.message)
//                }
//            }
//
//        }
    }

}