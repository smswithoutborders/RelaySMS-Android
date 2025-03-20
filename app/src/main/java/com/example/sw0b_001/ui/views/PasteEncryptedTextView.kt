package com.example.sw0b_001.ui.views

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.Bridges.Bridges
import com.example.sw0b_001.Models.NavigationFlowHandler
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.BridgeViewScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasteEncryptedTextView(
    platformsViewModel: PlatformsViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var pastedText by remember { mutableStateOf("") }
    val isDecryptButtonEnabled = pastedText.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Navigate back to inbox screen"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.paste_encrypted_text_into_this_box),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.this_is_an_example_of_an_encrypted_message_that_you_would_paste_here),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = stringResource(R.string.relaysms_reply_please_paste_this_entire_message_in_your_relaysms_app),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "3AAAAGUoAAAAAAAAAAAAAADN2pJG+g5bNt1ziT84plbY-cgwbbp+PbQHBf7ekxkOO...",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pastedText,
                onValueChange = { pastedText = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.click_to_paste)) },
                enabled = true,
                readOnly = false,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Bridges.decryptIncomingMessages(
                        context,
                        pastedText,
                        onSuccessCallback = {
                            val scope = CoroutineScope(Dispatchers.Main).launch {
//                            navController.popBackStack()
                                platformsViewModel.message = it
                                navController.navigate(BridgeViewScreen)
                            }
                        }
                    ) {
                        val scope = CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                it,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                enabled = isDecryptButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.decrypt_message))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PasteTextViewPreview() {
    PasteEncryptedTextView(
        platformsViewModel = PlatformsViewModel(),
        navController = NavController(LocalContext.current)
    )
}