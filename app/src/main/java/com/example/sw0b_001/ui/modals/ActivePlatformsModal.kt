package com.example.sw0b_001.ui.modals

import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.AvailablePlatformsView
import com.example.sw0b_001.ui.views.PlatformListContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePlatformsModal(
    sendNewMessageRequested: Boolean,
    platformsViewModel: PlatformsViewModel,
    navController: NavController,
    isCompose: Boolean = false,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )
    val scope = rememberCoroutineScope()
    var showSelectAccountModal by remember { mutableStateOf(false) }

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
                platformsViewModel = platformsViewModel,
                isCompose = isCompose
            ) {
                onDismiss()
            }
        }

    }

    if (showSelectAccountModal) {
        SelectAccountModal(
            platformsViewModel = platformsViewModel,
            onDismissRequest = {
                showSelectAccountModal = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ActivePlatformsModalPreview() {
    AppTheme {
        ActivePlatformsModal(
            sendNewMessageRequested = true,
            platformsViewModel = PlatformsViewModel(),
            onDismiss = {},
            navController = NavController(LocalContext.current)
        )
    }
}
