package com.example.sw0b_001.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.afkanerd.smswithoutborders_libsmsmms.extensions.pulseLoading
import com.example.sw0b_001.R

@Preview(showBackground = true)
@Composable
fun PulsingMessagePlaceholder() {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = MaterialTheme.colorScheme.secondary
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .pulseLoading(isLoading = true, color)
        )

        Spacer(Modifier.padding(start=12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Long message line
            Row {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .pulseLoading(isLoading = true, color)
                )

                Spacer(Modifier.padding(start=32.dp))
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .pulseLoading(isLoading = true, color)
                        .weight(1f)
                )
            }
            // Short message line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .pulseLoading(isLoading = true, color)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RmailAlertDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onTryItCallback: () -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                stringResource(R.string.you_can_send_a_quick_mail_without_verifying_or_adding_accounts_using_relaysms_mail),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onTryItCallback,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.try_it),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.padding(8.dp))
            Text(
                text = AnnotatedString.fromHtml(
                    ContextCompat.getString(
                        context,
                        R.string.you_can_always_find_the_option_when_you_click_compose)),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

