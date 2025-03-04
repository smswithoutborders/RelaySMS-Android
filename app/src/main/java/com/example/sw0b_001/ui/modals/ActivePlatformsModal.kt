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
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.PlatformData
import com.example.sw0b_001.ui.views.PlatformListContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePlatformsModal(
    platforms: List<PlatformData> = testPlatforms,
    onDismiss: () -> Unit,
    navController: NavController,
    isCompose: Boolean = false
) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )
    val scope = rememberCoroutineScope()
    var showSelectAccountModal by remember { mutableStateOf(false) }
    var selectedPlatform by remember { mutableStateOf<PlatformData?>(null) }

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                onDismiss()
            }
        },
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if(isCompose) {"Send messages"} else "Active Platforms",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = if(isCompose)
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom=16.dp)
                else Modifier
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                PlatformListContent(
                    platforms = platforms,
                    filterPlatforms = true,
                    onPlatformClick = { platform ->
                        when (platform.platformName) {
                            "Gmail" -> navController.navigate(Screen.EmailCompose.withIsDefault(false))
                            "Telegram" -> navController.navigate(Screen.MessageCompose.route)
                            "X" -> navController.navigate(Screen.TextCompose.route)
                            "RelaySMS" -> navController.navigate(Screen.EmailCompose.withIsDefault(true))
                            else -> {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    onDismiss()
                                }
                            }
                        }
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onDismiss()
                        }
                    }
                )
            }
        }
    }

    if (showSelectAccountModal && selectedPlatform != null) {
        SelectAccountModal(
            platform = selectedPlatform!!,
            navController = navController,
            onDismissRequest = {
                showSelectAccountModal = false
                selectedPlatform = null
            }
        )
    }
}

val testPlatforms = listOf(
    PlatformData(R.drawable.gmail, "Gmail", true),
    PlatformData(R.drawable.telegram, "Telegram", true),
    PlatformData(R.drawable.x_icon, "X", true),
)

@Preview(showBackground = true)
@Composable
fun ActivePlatformsModalPreview() {
    AppTheme {

        ActivePlatformsModal(
            platforms = testPlatforms,
            onDismiss = {},
            navController = NavController(LocalContext.current)
        )
    }
}
