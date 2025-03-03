package com.example.sw0b_001.ui.appbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.sw0b_001.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayClientsAppBar(
    navController: NavController,
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "Gateway Clients",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
//        navigationIcon = {
//            IconButton(onClick = {navController.popBackStack()}) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                    contentDescription = "Back"
//                )
//            }
//        },
        actions = {
            IconButton(onClick = { TODO("Implement refresh") }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Menu"
                )
            }
//            DropdownMenu(
//                expanded = showMenu,
//                onDismissRequest = { showMenu = false }
//            ) {
//                DropdownMenuItem(
//                    text = { Text("Refresh") },
//                    onClick = {
//                        TODO("Add functionality to refresh gateway clients")
////                        showMenu = false
//                    },
//                    leadingIcon = {
//                        Icon(
//                            imageVector = Icons.Filled.Refresh,
//                            contentDescription = "Refresh"
//                        )
//                    }
//                )
//            }
        },
        colors = TopAppBarDefaults.topAppBarColors()
    )
}

@Preview(showBackground = true)
@Composable
fun GatewayClientsAppBarPreview() {
    AppTheme {
        GatewayClientsAppBar(navController = NavController(LocalContext.current))
    }
}