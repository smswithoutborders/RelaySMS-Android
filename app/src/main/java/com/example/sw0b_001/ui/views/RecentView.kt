package com.example.sw0b_001.ui.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.sw0b_001.data.models.Bridges
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Composers
import com.example.sw0b_001.data.models.EncryptedContent
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.navigation.BridgeViewScreen
import com.example.sw0b_001.ui.navigation.EmailViewScreen
import com.example.sw0b_001.ui.navigation.MessageViewScreen
import com.example.sw0b_001.ui.navigation.TextViewScreen
import com.example.sw0b_001.ui.theme.AppTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecentView(
    navController: NavController,
    messagesViewModel: MessagesViewModel,
    platformsViewModel: PlatformsViewModel,
    isLoggedIn: Boolean = false,
    tabRequestedCallback: () -> Unit
) {
    val context = LocalContext.current
    
    LaunchedEffect(true) { 
        messagesViewModel.message = null
    }

    var sendNewMessageRequested by remember { mutableStateOf(false) }

    val messagesPagingSource = messagesViewModel.getMessages(context = context)
    val messages = messagesPagingSource.collectAsLazyPagingItems()

    val platforms: LiveData<List<AvailablePlatforms>> = platformsViewModel.getAvailablePlatforms(context)
    val platformsList by platforms.observeAsState(initial = emptyList())

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

                    val platform = platformsList.find { it.name == message.platformName }
                    val logo =
                        platform?.logo?.let { BitmapFactory
                            .decodeByteArray(it, 0, it.size) }

                    RecentMessageCard(
                        message = message, 
                        logo = logo,
                        onClickCallback = { clickedMessage ->
                            messagesViewModel.message = clickedMessage
                            when (clickedMessage.type?.uppercase()) {
                                Platforms.ServiceTypes.EMAIL.name -> {
                                    navController.navigate(EmailViewScreen)
                                }
                                Platforms.ServiceTypes.BRIDGE.name -> {
                                    navController.navigate(BridgeViewScreen)
                                }
                                Platforms.ServiceTypes.TEXT.name -> {
                                    navController.navigate(TextViewScreen)
                                }
                                Platforms.ServiceTypes.MESSAGE.name -> {
                                    navController.navigate(MessageViewScreen)
                                }
                                else -> {
                                    Toast.makeText(context,
                                        context.getString(R.string.something_went_wrong),
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    )
                }
            }
        }
        else if(messages.loadState.isIdle || LocalInspectionMode.current) {
            GetStartedView(
                navController = navController,
                loggedIn = isLoggedIn
            )
        }

        if (sendNewMessageRequested) {
            ActivePlatformsModal(
                sendNewMessageRequested = sendNewMessageRequested,
                navController = navController,
                isCompose = true
            ) {
                sendNewMessageRequested = false
            }
        }
    }
}

@Composable
fun GetMessageAvatar(logo: Bitmap? = null) {
    val context = LocalContext.current
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
    message: EncryptedContent,
    logo: Bitmap? = null,
    onClickCallback: (EncryptedContent) -> Unit,
) {
    var text by remember { mutableStateOf("" ) }
    var heading by remember { mutableStateOf( "") }
    var subHeading by remember { mutableStateOf( "") }

    when(message.type?.uppercase(Locale.getDefault())) {
        Platforms.ServiceTypes.EMAIL.name -> {
            val contentBytes = Base64.decode(message.encryptedContent!!, Base64.DEFAULT)
            val decomposed = Composers.EmailComposeHandler
                .decomposeMessage(
                    contentBytes,
                    imageLength = message.imageLength,
                    textLength = message.textLength,
                    isBridge = message.type == Platforms.ServiceTypes.BRIDGE.name
                )
            heading = message.fromAccount ?: "Email"
            subHeading = decomposed.subject.value
            text = decomposed.body.value
        }
        Platforms.ServiceTypes.BRIDGE_INCOMING.name -> {
            TODO()
//            val decomposed = TODO()
//            heading = message.fromAccount ?: "RelaySMS"
//            subHeading = decomposed.subject
//            text = decomposed.body
        }
        Platforms.ServiceTypes.BRIDGE.name -> {
            val decomposed = Composers.EmailComposeHandler.decomposeMessage(
                Base64.decode(message.encryptedContent, Base64.DEFAULT),
                message.imageLength,
                message.textLength,
                true
            )
            heading = message.fromAccount ?: "RelaySMS"
            subHeading = decomposed.subject.value
            text = decomposed.body.value
        }
        Platforms.ServiceTypes.TEXT.name -> {
            try {
                val contentBytes = Base64.decode(message.encryptedContent!!,
                    Base64.DEFAULT)
                val decomposed = Composers.TextComposeHandler
                    .decomposeMessage(contentBytes)
                heading = decomposed.from.value ?: ""
                subHeading = ""
                text = decomposed.text.value
            } catch (e: Exception) {
                e.printStackTrace()
                heading = message.fromAccount ?: stringResource(R.string.text_message)
                subHeading = ""
                text = stringResource(R.string.message_content_could_not_be_displayed)
            }
        }
        Platforms.ServiceTypes.MESSAGE.name -> {
            try {
                val contentBytes = Base64.decode(message.encryptedContent!!,
                    Base64.DEFAULT)
                val decomposed = Composers.MessageComposeHandler
                    .decomposeMessage(contentBytes)

                if (message.fromAccount == decomposed.from.value) {
                    heading = decomposed.to.value
                } else {
                    heading = decomposed.from.value ?: "RelaySMS"
                }
                subHeading = ""
                text = decomposed.message.value
            } catch (e: Exception) {
                e.printStackTrace()
                heading = message.fromAccount ?: stringResource(R.string.message_)
                subHeading = ""
                text = stringResource(R.string.message_content_could_not_be_displayed)
            }
        }
    }

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
                    style = if (message.type == Platforms.ServiceTypes.TEXT.name) {
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
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
        
//        OutlinedCard(
//            onClick = {
//                onClickCallback(message)
//            },
//            modifier = Modifier
//                .fillMaxWidth(),
//            shape = RoundedCornerShape(8.dp),
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                GetMessageAvatar(logo)
//
//                Spacer(modifier = Modifier.width(16.dp))
//                Column(modifier = Modifier.weight(1f)) {
//                    // Heading Text
//                    Text(
//                        heading,
//                        style = if (message.type == Platforms.ServiceTypes.TEXT.name) {
//                            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
//                        } else {
//                            MaterialTheme.typography.bodyLarge
//                        },
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    // Subheading Text
//                    if (message.encryptedContent != null) {
//                        Text(
//                            subHeading,
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                    }
//                    // Message Preview
//                    Text(
//                        text = text,
//                        style = MaterialTheme.typography.bodySmall,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//
//                // Date
//                Text(
//                    text = Helpers.formatDate(LocalContext.current, message.date),
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentScreenPreview() {
    AppTheme(darkTheme = false) {
        val encryptedContent = EncryptedContent()
        encryptedContent.id = 0
        encryptedContent.type = "email"
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.platformName = "gmail"
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "This is an encrypted content"
        RecentView(
            navController = rememberNavController(),
            messagesViewModel = remember { MessagesViewModel() },
            platformsViewModel = remember { PlatformsViewModel() },
            isLoggedIn = true
        ) {}
    }
}


@Preview(showBackground = true)
@Composable
fun RecentScreenMessages_Preview() {
    AppTheme(darkTheme = false) {
        val encryptedContent = EncryptedContent()
        encryptedContent.id = 0
        encryptedContent.type = "email"
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.platformName = "gmail"
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content"

        val text = EncryptedContent()
        text.id = 1
        text.type = "text"
        text.date = System.currentTimeMillis()
        text.platformName = "twitter"
        text.fromAccount = "@relaysms.me"
        text.gatewayClientMSISDN = "+237123456789"
        text.encryptedContent = "@relaysms.me:Hello world"

        val message = EncryptedContent()
        message.id = 2
        message.type = "message"
        message.date = System.currentTimeMillis()
        message.platformName = "telegram"
        message.fromAccount = "+237123456789"
        message.gatewayClientMSISDN = "+237123456789"
        message.encryptedContent = "+123456789:+237123456789:hello Telegram"

        RecentView(
            navController = rememberNavController(),
            messagesViewModel = remember { MessagesViewModel() },
            platformsViewModel = remember { PlatformsViewModel() },
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun RecentsCardPreview() {
    AppTheme(darkTheme = false) {
        val encryptedContent = EncryptedContent()
        encryptedContent.id = 0
        encryptedContent.type = "email"
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.platformName = "gmail"
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content"
        RecentMessageCard(
            message = encryptedContent,
            onClickCallback = {},
        )
    }
}
