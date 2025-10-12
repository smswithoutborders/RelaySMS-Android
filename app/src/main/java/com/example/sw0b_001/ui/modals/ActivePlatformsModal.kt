package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.AvailablePlatformsView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePlatformsModal(
    sendNewMessageRequested: Boolean,
    navController: NavController,
    isLoggedIn: Boolean = false,
    isCompose: Boolean = false,
    isOnboarding: Boolean = false,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
//        initialValue = SheetValue.Expanded,
//        skipHiddenState = false,
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
            AvailablePlatformsView(
                navController = navController,
                isCompose = isCompose,
                isOnboarding = isOnboarding,
            ) {
                onDismiss()
            }
        }

    }

}

@Preview(showBackground = true)
@Composable
fun ActivePlatformsModalPreview() {
    AppTheme {
        ActivePlatformsModal(
            sendNewMessageRequested = true,
            onDismiss = {},
            navController = NavController(LocalContext.current)
        )
    }
}
