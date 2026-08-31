package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sw0b_001.data.models.SupportedPlatforms
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.ui.components.SelectAccountModalComponent
import com.example.sw0b_001.ui.theme.AppTheme
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1PayloadsSupportedProtocols

// Data class to represent an account
data class Account(
    val profilePhoto: Int?,
    val platformName: String,
    val accountIdentifier: String,
    val subtext: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAccountModal(
    accounts: List<Tokens>,
    supportedPlatform: SupportedPlatforms,
    isCompose: Boolean,
    isComposeOffline: Boolean,
    onRemoveAccountCallback: ((Tokens) -> Unit)? = null,
    onAddAccountCallback: (() -> Unit)? = null,
    onAccountSelected: ((Tokens) -> Unit)? = null,
    onDismissRequest: () -> Unit,
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    Box(Modifier
        .fillMaxSize()
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            dragHandle = null
        ) {
            SelectAccountModalComponent(
                accounts = accounts,
                isCompose = isCompose,
                isComposeOffline = isComposeOffline,
                supportedPlatform = supportedPlatform,
                onAccountSelected = { token ->
                    onAccountSelected?.invoke(token)
                    onDismissRequest()
                },
                onSheetHideCallback = onDismissRequest,
                onAddAccountCallback = onAddAccountCallback ?: {},
                onRemoveAccountCallback = onRemoveAccountCallback ?: {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectAccountModal_preview() {

    val tokens = listOf(
        Tokens(
            tokenId = 1,
            tokenHash = ByteArray(0),
            catId = V1ContentCategories.EMAIL,
            account = "sample@example.com",
            platformName = "gmail",
            date = System.currentTimeMillis()
        ),
        Tokens(
            tokenId = 2,
            tokenHash = ByteArray(0),
            catId = V1ContentCategories.TEXT,
            account = "sample@example.com",
            platformName = "bluesky",
            date = System.currentTimeMillis()
        )
    )

    val supportedPlatform = SupportedPlatforms(
        name = "rmail",
        display_name = "RelaySMS mail",
        supports_offline_first = true,
        cat_id = V1ContentCategories.EMAIL.value.toInt(),
        proto_id = V1PayloadsSupportedProtocols.O_AUTH20.value.toInt(),
        icon_svg = null,
        icon_png = null
    )
    AppTheme() {
        SelectAccountModal(
            accounts = tokens,
            supportedPlatform = supportedPlatform,
            isCompose = true,
            isComposeOffline = true
        ) { }
    }
}