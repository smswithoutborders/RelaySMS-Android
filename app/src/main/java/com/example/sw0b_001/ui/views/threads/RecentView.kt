package com.example.sw0b_001.ui.views.threads

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.data.models.Payloads
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.navigation.EmailViewScreen
import com.example.sw0b_001.ui.navigation.MessageViewScreen
import com.example.sw0b_001.ui.navigation.TextViewScreen
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import uniffi.relaysms_spec_payload.V1ContentCategories

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecentView(
    navController: NavController,
    payloadsViewModel: PayloadsViewModel,
    tokensViewModel: TokensViewModel,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    isLoggedIn: Boolean = false,
    tabRequestedCallback: () -> Unit
) {
    var sendNewMessageRequested by remember { mutableStateOf(false) }

    val messages = payloadsViewModel.get().collectAsLazyPagingItems()

    val listState = rememberLazyListState()
    Box(Modifier.fillMaxSize()
    ) {
        if ((LocalInspectionMode.current || messages.loadState.isIdle) && messages.itemCount > 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                items(
                    count = messages.itemCount,
                    key = messages.itemKey { it.id }
                ) { index ->
                    val message = messages[index]!!

                    RecentMessageCard(
                        message = message, 
//                        logo = logo,
                        onClickCallback = { clickedMessage ->
                            when (clickedMessage.catId) {
                                V1ContentCategories.BRIDGE,
                                V1ContentCategories.EMAIL -> {
                                    navController.navigate(EmailViewScreen(
                                        cat = clickedMessage.catId,
                                        messageId = message.id
                                    ))
                                }
                                V1ContentCategories.TEXT -> {
                                    navController.navigate(TextViewScreen(
                                        messageId = message.id
                                    ))
                                }
                                V1ContentCategories.MESSAGE -> {
                                    navController.navigate(MessageViewScreen(
                                        messageId = message.id
                                    ))
                                }
                            }
                        },
                    )
                }
            }
        }
        else if(messages.loadState.isIdle || LocalInspectionMode.current) {
            GetStartedView( navController = navController, )
        }

        if (sendNewMessageRequested) {
            ActivePlatformsModal(
                sendNewMessageRequested = sendNewMessageRequested,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
                navController = navController,
                isCompose = true,
                tokensViewModel = tokensViewModel,
                isLoggedIn = isLoggedIn,
            ) {
                sendNewMessageRequested = false
            }
        }
    }
}

@Composable
fun GetMessageAvatar(logo: Bitmap? = null) {
    val imageSize = 38.dp
    if(LocalInspectionMode.current || logo == null) {
        Image(
            painterResource(R.drawable.relaysms_icon_default_shape),
            contentDescription = stringResource(R.string.avatar_image),
            modifier = Modifier.size(imageSize)
        )
    }
    else {
        Image(
            bitmap = logo.asImageBitmap(),
            contentDescription = stringResource(R.string.avatar_image),
            modifier = Modifier.size(imageSize)
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentMessageCard(
    message: Payloads,
    logo: Bitmap? = null,
    onClickCallback: (Payloads) -> Unit,
) {
    var text by remember { mutableStateOf("" ) }
    var heading by remember { mutableStateOf( "") }
    var subHeading by remember { mutableStateOf( "") }

//    when(message.type?.uppercase(Locale.getDefault())) {
//        Platforms.ServiceTypes.EMAIL.name -> {
//            val contentBytes = Base64.decode(message.body!!, Base64.DEFAULT)
////            val decomposed = Composers.EmailComposeHandler
////                .decomposeMessage(
////                    contentBytes,
////                    imageLength = message.imageLength,
////                    textLength = message.textLength,
////                    isBridge = message.type == Platforms.ServiceTypes.BRIDGE.name
////                )
////            heading = message.fromAccount ?: "Email"
////            subHeading = decomposed.subject.value
////            text = decomposed.body.value
//        }
//        Platforms.ServiceTypes.BRIDGE_INCOMING.name -> {
//            TODO()
////            val decomposed = TODO()
////            heading = message.fromAccount ?: "RelaySMS"
////            subHeading = decomposed.subject
////            text = decomposed.body
//        }
//        Platforms.ServiceTypes.BRIDGE.name -> {
////            val decomposed = Composers.EmailComposeHandler.decomposeMessage(
////                Base64.decode(message.body, Base64.DEFAULT),
////                message.imageLength,
////                message.textLength,
////                true
////            )
//////            heading = message.fromAccount ?: "RelaySMS"
////            subHeading = decomposed.subject.value
////            text = decomposed.body.value
//        }
//        Platforms.ServiceTypes.TEXT.name -> {
//            try {
//                val contentBytes = Base64.decode(message.body!!,
//                    Base64.DEFAULT)
//                val decomposed = Composers.TextComposeHandler
//                    .decomposeMessage(contentBytes)
//                heading = decomposed.from.value ?: ""
//                subHeading = ""
//                text = decomposed.text.value
//            } catch (e: Exception) {
//                e.printStackTrace()
////                heading = message.fromAccount ?: stringResource(R.string.text_message)
//                subHeading = ""
//                text = stringResource(R.string.message_content_could_not_be_displayed)
//            }
//        }
//        Platforms.ServiceTypes.MESSAGE.name -> {
//            try {
//                val contentBytes = Base64.decode(message.body!!,
//                    Base64.DEFAULT)
//                val decomposed = Composers.MessageComposeHandler
//                    .decomposeMessage(contentBytes)
//
////                if (message.fromAccount == decomposed.from.value) {
////                    heading = decomposed.to.value
////                } else {
////                    heading = decomposed.from.value ?: "RelaySMS"
////                }
//                subHeading = ""
//                text = decomposed.message.value
//            } catch (e: Exception) {
//                e.printStackTrace()
////                heading = message.fromAccount ?: stringResource(R.string.message_)
//                subHeading = ""
//                text = stringResource(R.string.message_content_could_not_be_displayed)
//            }
//        }
//    }

    Column {
        ListItem(
            modifier = Modifier
                .combinedClickable(
                    hapticFeedbackEnabled = true,
                    onLongClick = {},
                    onClick = { onClickCallback(message) }
                )
                .fillMaxWidth(),
            headlineContent = {
                Text(
                    subHeading,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            overlineContent = {
                Text(
                    heading,
//                    style = if (message.type == Platforms.ServiceTypes.TEXT.name) {
//                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
//                    } else {
//                        MaterialTheme.typography.bodyLarge
//                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                GetMessageAvatar(logo)
            },
            trailingContent = {
                Text(
                    text = Helpers.formatDate(LocalContext.current, message.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun RecentScreenPreview() {
//    AppTheme(darkTheme = false) {
//        val messages = Messages()
//        messages.id = 0
//        messages.type = "email".encodeToByteArray()[0]
//        messages.date = System.currentTimeMillis()
//        messages.fromAccount = "developers@relaysms.me".encodeToByteArray()
//        messages.body = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content".encodeToByteArray()
//
//        val context = LocalContext.current
//        RecentView(
//            navController = rememberNavController(),
//            messagesViewModel = remember { MessagesViewModel() },
//            storedPlatformsViewModel = remember { StoredPlatformsViewModel(context) },
//            isLoggedIn = true
//        ) {}
//    }
//}
//

//@Preview(showBackground = true)
//@Composable
//fun RecentScreenMessages_Preview() {
//    AppTheme(darkTheme = false) {
//        val messages = Messages()
//        messages.id = 0
//        messages.type = "email".encodeToByteArray()[0]
//        messages.date = System.currentTimeMillis()
//        messages.fromAccount = "developers@relaysms.me".encodeToByteArray()
//        messages.body = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content".encodeToByteArray()
//
//        val context = LocalContext.current
//        RecentView(
//            navController = rememberNavController(),
//            messagesViewModel = remember { MessagesViewModel() },
//            storedPlatformsViewModel = remember { StoredPlatformsViewModel(context) },
//        ) {}
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun RecentsCardPreview() {
//    AppTheme(darkTheme = false) {
//        val messages = Messages()
//        messages.id = 0
//        messages.type = "email".encodeToByteArray()[0]
//        messages.date = System.currentTimeMillis()
//        messages.fromAccount = "developers@relaysms.me".encodeToByteArray()
//        messages.body = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content".encodeToByteArray()
//        RecentMessageCard(
//            message = messages,
//            onClickCallback = {},
//        )
//    }
//}
