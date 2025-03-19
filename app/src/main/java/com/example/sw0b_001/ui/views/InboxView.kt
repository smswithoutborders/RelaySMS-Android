package com.example.sw0b_001.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.BridgeViewScreen
import com.example.sw0b_001.ui.navigation.EmailViewScreen
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.theme.AppTheme


@Composable
fun InboxView(
    _messages: List<EncryptedContent> = emptyList<EncryptedContent>(),
    messagesViewModel: MessagesViewModel,
    platformsViewModel: PlatformsViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    val messages: List<EncryptedContent> = if(LocalInspectionMode.current) _messages
    else messagesViewModel.getInboxMessages(context).observeAsState(emptyList()).value

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (messages.isEmpty()) {
            EmptyInboxContent(onPasteNewMessageClicked = {
                navController.navigate(PasteEncryptedTextScreen)
            })
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    RecentMessageCard(
                        message,
                        onClickCallback = {
                            platformsViewModel.message = message
                            navController.navigate(BridgeViewScreen)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyInboxContent(onPasteNewMessageClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_message),
            contentDescription = "Inbox Icon",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_messages_in_inbox),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))
        Button(onClick = { onPasteNewMessageClicked() }) {
            Text(stringResource(R.string.paste_new_incoming_message))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InboxViewEmptyPreview() {
    AppTheme {
        InboxView(
            messagesViewModel = MessagesViewModel(),
            platformsViewModel = PlatformsViewModel(),
            navController = NavController(LocalContext.current),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InboxScreenMessages_Preview() {
    AppTheme(darkTheme = false) {
        val encryptedContent = EncryptedContent()
        encryptedContent.id = 0
        encryptedContent.type = "email"
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.platformName = "gmail"
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "dev@relaysms.me:::subject here:This is an encrypted content"

        val text = EncryptedContent()
        text.id = 1
        text.type = "text"
        text.date = System.currentTimeMillis()
        text.platformName = "twitter"
        text.fromAccount = "@relaysms.me"
        text.gatewayClientMSISDN = "+237123456789"
        text.encryptedContent = "@relaysms.me:Hello world"
        InboxView(
            _messages = listOf(encryptedContent, text),
            messagesViewModel = MessagesViewModel(),
            platformsViewModel = PlatformsViewModel(),
            navController = rememberNavController(),
        )
    }
}

