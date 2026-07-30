package com.example.sw0b_001.ui.views.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.data.models.Payloads
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.compose.toUtf8String

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailsView(message: Payloads?) {
    val context = LocalContext.current

//    var from by remember { mutableStateOf( account?.platformName ) }
    var to by remember(message) {
        mutableStateOf( message?.content?.getTo()?.toUtf8String() ?: "") }
    var body by remember(message) {
        mutableStateOf(message?.content?.getBody()?.toUtf8String() ?: "") }
    var date by remember(message) { mutableLongStateOf(message?.date ?: 0L) }

    Column( modifier = Modifier .fillMaxSize() ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.generic_avatar),
                contentDescription = stringResource(R.string.sender_avatar),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                // Recipient Number
                Text(
                    text = "${stringResource(R.string.to)}: $to",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                // Date
                Text(
                    text = Helpers.formatDate(context, date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MessageDetailsPreview() {
    AppTheme(darkTheme = false) {
//        val message = Messages()
//        message.id = 2
//        message.type = "message"
//        message.date = System.currentTimeMillis()
//        message.platformName = "telegram"
//        message.fromAccount = "+237123456789"
//        message.gatewayClientMSISDN = "+237123456789"
//        message.body = "+123456789:+237123456789:hello Telegram"
//
//        val storedPlatformsViewModel = remember{ StoredPlatformsViewModel() }
//        val messagesViewModel = remember{ MessagesViewModel() }
//        messagesViewModel.message = message
//
//        MessageDetailsView(
//            storedPlatformsViewModel = storedPlatformsViewModel,
//            messagesViewModel = messagesViewModel,
//            navController = NavController(LocalContext.current)
//        )
    }
}