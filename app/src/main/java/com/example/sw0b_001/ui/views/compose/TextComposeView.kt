package com.example.sw0b_001.ui.views.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import kotlinx.serialization.Serializable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextComposeView(
    body: String,
    bodyCallback: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = body,
            onValueChange = bodyCallback,
            label = {
                Text(stringResource(R.string.what_s_happening),
                    style = MaterialTheme.typography.bodyMedium)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}


@Serializable
data class ReliabilityTestRequestPayload(val test_start_time: String)

@Serializable
data class ReliabilityTestResponsePayload(
    val message: String,
    val test_id: Int,
    val test_start_time: Int,
)

//@Preview(showBackground = true)
//@Composable
//fun TextComposePreview() {
//    AppTheme(darkTheme = false) {
//        TextComposeView(
//            textContent = Composers.TextComposeHandler.TextContent(),
//            serviceType = Platforms.ServiceTypes.TEXT
//        )
//    }
//}