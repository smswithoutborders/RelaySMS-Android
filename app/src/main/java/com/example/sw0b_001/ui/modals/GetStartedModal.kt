package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.GetStartedView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetStartedModal(
    getStartedRequested: Boolean,
    navController: NavController,
    isLoggedIn: Boolean,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )
    val scope = rememberCoroutineScope()

    if(getStartedRequested) {
        ModalBottomSheet(
            onDismissRequest = {
                scope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion { onDismiss() }
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth()
        ) {
            GetStartedView(navController, isLoggedIn)
        }

    }

}

@Preview(showBackground = true)
@Composable
fun GetStartedModalPreviewNotLoggedIn() {
    AppTheme {
        GetStartedModal (
            getStartedRequested = true,
            navController = rememberNavController(),
            false,
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GetStartedModalPreview() {
    AppTheme {
        GetStartedModal (
            getStartedRequested = true,
            navController = rememberNavController(),
            true,
            onDismiss = {},
        )
    }
}
