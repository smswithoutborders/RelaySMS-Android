package com.example.sw0b_001.ui.views.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.Messages
import com.example.sw0b_001.ui.views.compose.toUtf8String

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailsView(message: Messages?) {
    val context = LocalContext.current

    var from by remember { mutableStateOf("") }

    var to by remember(message) {
        mutableStateOf( message?.content?.getTo()?.toUtf8String() ?: "") }

    var subject by remember(message) { mutableStateOf(
        message?.content?.getSubject()?.toUtf8String() ?: "") }

    var body by remember(message) {
        mutableStateOf(message?.content?.getBody()?.toUtf8String() ?: "") }

    var date by remember(message) { mutableLongStateOf(message?.date ?: 0L) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Subject
        if(subject.isNotEmpty() || LocalInspectionMode.current) {
            Text(
                text = subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Sender Avatar
            Image(
                painter = painterResource(R.drawable.round_person_24),
                contentDescription = stringResource(R.string.sender_avatar),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                // Sender Email
                Text(
                    text = from ?: "",
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
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))

        EmailDetailsRow(label = stringResource(R.string.to), email = to)

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

@Composable
fun EmailDetailsRow(label: String, email: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start=8.dp)
            )
        }
    }
}

@Composable
@Preview
fun EmailDetailsPreview() {
    AppTheme(darkTheme = false) {
//        val messages = Messages()
//        messages.id = 0
//        messages.type = "email"
//        messages.date = System.currentTimeMillis()
//        messages.platformName = "gmail"
//        messages.fromAccount = "developers@relaysms.me"
//        messages.gatewayClientMSISDN = "+237123456789"
//        messages.body = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content"
//
//        val storedPlatformsViewModel = remember{ StoredPlatformsViewModel() }
//        val messagesViewModel = remember{ MessagesViewModel() }
//        messagesViewModel.message = messages
//        EmailDetailsView(
//            storedPlatformsViewModel=storedPlatformsViewModel,
//            messagesViewModel= messagesViewModel,
//            imageViewModel = remember{ ImageViewModel() },
//            navController = rememberNavController()
//        )
    }
}