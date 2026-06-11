package com.example.sw0b_001.ui.views.details

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.components.AttachImageView
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import uniffi.relaysms_spec_payload.V1ContentCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsInterfaceView(
    navController: NavController,
    cat: V1ContentCategories,
    tokensViewModel: TokensViewModel,
    payloadsViewModel: PayloadsViewModel,
    imageViewModel: ImageViewModel,
    messageId: Long
) {

    val message by payloadsViewModel.message.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        payloadsViewModel.get(messageId)
    }

    var imageBitmap by remember(message) { mutableStateOf(
        if(message?.content != null) {
            val attachment = message!!.content.getAttachment()
            if(attachment != null) {
                BitmapFactory.decodeByteArray(attachment, 0, attachment.size)
            } else null
        } else null
    )}


    val scrollState = rememberScrollState() // Remember the scroll state

    Scaffold(
        topBar = {
            RelayAppBar(navController = navController, {
                navController.navigate(
                    ComposeScreen(
                        cat = cat,
                        messageId = messageId
                    )
                )
            }) {
                payloadsViewModel.delete(messageId)
                navController.popBackStack()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when(cat) {
                V1ContentCategories.EMAIL,
                V1ContentCategories.BRIDGE -> {
                    EmailDetailsView(message)
                }
                V1ContentCategories.MESSAGE -> {
                    MessageDetailsView(message)
                }
                V1ContentCategories.TEXT -> {
                    TextDetailsView(message)
                }
            }

            imageBitmap?.let {
                Spacer(Modifier.padding(24.dp))
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    AttachImageView(
                        it,
                        onCancelCallback = null
                    ) { }
                }
            }
        }
    }
}