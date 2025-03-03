package com.example.sw0b_001.ui.views.details

import android.os.Parcelable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.MessageType
import kotlinx.android.parcel.Parcelize

// Data class for Gmail details

//data class EmailDetails(
//    val subject: String,
//    val senderAvatar: Int,
//    val senderEmail: String,
//    val to: List<String>,
//    val cc: List<String>,
//    val bcc: List<String>,
//    val date: String,
//    val fullText: String
//)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailsView(
    message: EncryptedContent,
    navController: NavController
) {
//    val emailDetails = EmailDetails(
//        subject = message.headingText,
//        senderAvatar = message.platformLogo,
//        senderEmail = message.subHeadingText ?: "",
//        to = listOf("recipient1@example.com", "recipient2@example.com"),
//        cc = listOf("cc1@example.com"),
//        bcc = listOf("bcc1@example.com", "bcc2@example.com"),
//        date = message.date,
//        fullText = message.messagePreview
//    )
    
    Scaffold(
        topBar = {
            RelayAppBar(screenName = "Email Details", navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Subject
            Text(
                text = "Email subject",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sender Avatar
//                Image(
//                    painter = painterResource(id = emailDetails.senderAvatar),
//                    contentDescription = "Sender Avatar",
//                    modifier = Modifier
//                        .size(48.dp)
//                        .clip(CircleShape)
//                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    // Sender Email
//                    Text(
//                        text = emailDetails.senderEmail,
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onBackground
//                    )
//                    // Date
//                    Text(
//                        text = emailDetails.date,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onBackground
//                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { TODO("Implement edit functionality") }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = { TODO("Implement delete functionality") }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

// To, CC, BCC
//            if (emailDetails.to.isNotEmpty()) {
//                EmailDetailsRow(label = "To", emails = emailDetails.to)
//            }
//            if (emailDetails.cc.isNotEmpty()) {
//                EmailDetailsRow(label = "Cc", emails = emailDetails.cc)
//            }
//            if (emailDetails.bcc.isNotEmpty()) {
//                EmailDetailsRow(label = "Bcc", emails = emailDetails.bcc)
//            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            // Full Text
//            Text(
//                text = emailDetails.fullText,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onBackground
//            )
        }
    }
}

@Composable
fun EmailDetailsRow(label: String, emails: List<String>) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        emails.forEach { email ->
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

//@Composable
//@Preview
//fun EmailDetailsPreview() {
//    AppTheme(darkTheme = false) {
//        EmailDetailsView(
//            message = RecentMessage(
//                platformLogo = R.drawable.gmail,
//                platformName = "Gmail",
//                headingText = "Meeting Agenda",
//
//                subHeadingText = "sender@example.com",
//                messagePreview = "Hi team, Please find the agenda for our upcoming meeting.",
//                date = "Oct 26, 2023",
//                messageType = MessageType.GMAIL
//            ),
//            navController = NavController(LocalContext.current)
//        )
//    }
//}