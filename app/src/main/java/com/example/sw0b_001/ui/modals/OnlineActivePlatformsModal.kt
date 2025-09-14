package com.example.sw0b_001.ui.modals

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.AvailablePlatformsView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineActivePlatformsModal(
    showBottomSheet: Boolean,
    navController: NavController,
    isCompose: Boolean,
    isOnboarding: Boolean,
    onCompleteCallback: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AvailablePlatformsView(
                    navController = navController,
                    isCompose = isCompose,
                    isOnboarding = isOnboarding,
                    onCompleteCallback = onCompleteCallback,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}


@Preview
@Composable
fun OnlineActivePlatformsModalPreview() {
    AppTheme {
        OnlineActivePlatformsModal(
            true,
            navController = rememberNavController(),
            isCompose = false,
            isOnboarding = true,
            {},
        ){}
    }
}