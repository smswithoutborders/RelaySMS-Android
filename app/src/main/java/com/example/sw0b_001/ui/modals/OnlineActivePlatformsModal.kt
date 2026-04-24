package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.ui.viewModels.AccountsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.views.SupportedPlatformsView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineActivePlatformsModal(
    navController: NavController,
    showBottomSheet: Boolean,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    accountsViewModel: AccountsViewModel,
    isCompose: Boolean,
    isOnboarding: Boolean,
    isLoggedIn: Boolean,
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
                SupportedPlatformsView(
                    navController = navController,
                    supportedPlatformsViewModel = supportedPlatformsViewModel,
                    accountsViewModel = accountsViewModel,
                    isCompose = isCompose,
                    isOnboarding = isOnboarding,
                    onCompleteCallback = onCompleteCallback,
                    isLoggedIn = isLoggedIn,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}