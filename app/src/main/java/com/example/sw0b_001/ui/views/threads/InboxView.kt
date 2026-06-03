package com.example.sw0b_001.ui.views.threads

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.EmailViewScreen
import com.example.sw0b_001.ui.navigation.PasteEncryptedTextScreen
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import uniffi.relaysms_spec_payload.V1ContentCategories


@Composable
fun InboxView(
    navController: NavController,
    messagesViewModel: MessagesViewModel,
    tokensViewModel: TokensViewModel,
) {
    val messages by messagesViewModel.getInboxMessages().observeAsState(emptyList())

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
                            navController.navigate(
                                EmailViewScreen(
                                    V1ContentCategories.BRIDGE,
                                    messageId = message.id
                                )
                            )
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
