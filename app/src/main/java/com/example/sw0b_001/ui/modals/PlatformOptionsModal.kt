package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.PlatformData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformOptionsModal(
    platform: PlatformData,
    isActive: Boolean = true,
    isDefault: Boolean = false,
    onDismissRequest: () -> Unit,
    navController: NavController

) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(true) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = platform.icon),
                    contentDescription = platform.platformName,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isDefault) {
                        "Use your phone number to send out emails with your_phonenumber@relaySMS.me"
                    } else {
                        "Give RelaySMS permission to send ${platform.platformName} messages on your behalf when offline"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (isDefault) {
                    Button(
                        onClick = {
                            showBottomSheet = false
                            navController.navigate(Screen.EmailCompose.withIsDefault(true))
                                  },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Message")
                    }
                } else {
                    Button(
                        onClick = { TODO("Add add accounts functionality") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Account")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isActive) {
                        TextButton(
                            onClick = { TODO("Add remove accounts functionality") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Remove Accounts")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PlatformOptionsModalPreview() {
    AppTheme(darkTheme = false) {
        PlatformOptionsModal(
            platform = PlatformData(R.drawable.gmail, "Gmail", true),
            onDismissRequest = {},
            navController = NavController(LocalContext.current)
        )
    }
}