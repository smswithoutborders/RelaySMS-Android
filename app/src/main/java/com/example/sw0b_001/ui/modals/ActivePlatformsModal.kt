package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.sw0b_001.ui.viewModels.AccountsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.views.tabs.SupportedPlatformsView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePlatformsModal(
    navController: NavController,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    accountsViewModel: AccountsViewModel,
    sendNewMessageRequested: Boolean,
    isLoggedIn: Boolean = false,
    isCompose: Boolean = false,
    isOnboarding: Boolean = false,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = isLoggedIn
    )
    val scope = rememberCoroutineScope()

    if(sendNewMessageRequested) {
        ModalBottomSheet(
            onDismissRequest = {
                scope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion { onDismiss() }
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth()
        ) {
            SupportedPlatformsView(
                navController = navController,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
                accountsViewModel = accountsViewModel,
                isCompose = isCompose,
                isLoggedIn = isLoggedIn,
                isOnboarding = isOnboarding,
            )
        }

    }

}