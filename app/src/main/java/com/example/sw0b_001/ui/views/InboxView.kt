package com.example.sw0b_001.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
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
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Messages
import com.example.sw0b_001.ui.navigation.BridgeViewScreen
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.StoredPlatformsViewModel


@Composable
fun InboxView(
    _messages: List<Messages> = emptyList<Messages>(),
    messagesViewModel: MessagesViewModel,
    storedPlatformsViewModel: StoredPlatformsViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    val messages: List<Messages> = if(LocalInspectionMode.current) _messages
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
                contentPadding = PaddingValues(8.dp),
            ) {
                items(messages) { message ->
                    RecentMessageCard(
                        message,
                        onClickCallback = {
                            messagesViewModel.message = message
                            navController.navigate(BridgeViewScreen)
                        },
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
            contentDescription = stringResource(R.string.inbox_icon),
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
            Text(
                stringResource(R.string.paste_new_incoming_message),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InboxViewEmptyPreview() {
    val context = LocalContext.current
    AppTheme {
        InboxView(
            messagesViewModel = remember { MessagesViewModel() },
            storedPlatformsViewModel = remember { StoredPlatformsViewModel(context) },
            navController = NavController(LocalContext.current),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InboxScreenMessages_Preview() {
    AppTheme(darkTheme = false) {
        val messages = Messages()
        messages.id = 0
        messages.type = "email".encodeToByteArray()[0]
        messages.date = System.currentTimeMillis()
        messages.fromAccount = "developers@relaysms.me".encodeToByteArray()
        messages.body = "dev@relaysms.me:::subject here:This is an encrypted content".encodeToByteArray()

        val text = Messages()
        text.id = 1
        text.type = "text".encodeToByteArray()[0]
        text.date = System.currentTimeMillis()
        text.fromAccount = "@relaysms.me".encodeToByteArray()
        text.body = "@relaysms.me:Hello world".encodeToByteArray()
        val context = LocalContext.current
        InboxView(
            _messages = listOf(messages, text),
            messagesViewModel = remember { MessagesViewModel() },
            storedPlatformsViewModel = remember { StoredPlatformsViewModel(context) },
            navController = rememberNavController(),
        )
    }
}

