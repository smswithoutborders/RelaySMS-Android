package com.example.sw0b_001.ui.features

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFeatureModal(
    featureInfo: FeatureInfo,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true, // Or false, depending on your preference
        // confirmValueChange = { it != SheetValue.PartiallyExpanded } // Optional: prevent partial expansion
    )
    val scope = rememberCoroutineScope()
    // This modal is controlled by its presence in the composition,
    // so we don't need an internal `showBottomSheet` state here.
    // The caller will decide when to show it.

    ModalBottomSheet(
        onDismissRequest = {
            // We call onDismiss directly, which should also trigger
            // marking the feature as seen and removing the modal from composition.
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(), // Important for bottom sheet content
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            featureInfo.iconRes?.let { icon ->
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = stringResource(R.string.new_feature_icon_desc), // Add a generic description
                    modifier = Modifier.size(64.dp) // Adjusted size
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = stringResource(id = featureInfo.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = featureInfo.descriptionRes),
                style = MaterialTheme.typography.bodyLarge, // Slightly larger for better readability
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        // Ensure it's hidden before calling dismiss if there's a race condition
                        if (!sheetState.isVisible) {
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = stringResource(R.string.got_it), color = Color.White) // Generic "Got it"
            }
            // Add a spacer at the bottom if not using navigationBarsPadding on the Column
            // Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewFeatureModalPreview() {
    AppTheme {
        NewFeatureModal(
            featureInfo = FeatureInfo(
                id = "preview_feature",
                titleRes = R.string.preview_feature_title, // Add these to your strings.xml for preview
                descriptionRes = R.string.preview_feature_description,
                iconRes = R.drawable.relaysms_icon_default_shape // Use an existing drawable
            ),
            onDismiss = {}
        )
    }
}