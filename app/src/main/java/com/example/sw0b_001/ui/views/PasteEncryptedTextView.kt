package com.example.sw0b_001.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PasteEncryptedTextView(
    navController: NavController
) {
    var pastedText by remember { mutableStateOf("") }
    val isDecryptButtonEnabled = pastedText.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Paste encrypted text into this box...",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This is an example of an encrypted message that you would paste here...",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = "RelaySMS Reply Please paste this entire message in your RelaySMS app",
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
                .fillMaxWidth()
                .height(200.dp)
            ,
            placeholder = { Text("Click to paste") },
            enabled = false,
            readOnly = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { TODO("Handle decrypt") },
            enabled = isDecryptButtonEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Decrypt message")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PasteTextViewPreview() {
    PasteEncryptedTextView(
        navController = NavController(LocalContext.current)

    )
}