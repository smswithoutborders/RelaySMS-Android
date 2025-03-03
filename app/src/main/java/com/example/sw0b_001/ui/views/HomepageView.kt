package com.example.sw0b_001.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController

@Composable
fun HomepageView(navController: NavController) {

    var isLoggedIn by remember { mutableStateOf(false) }


    if (isLoggedIn) {
        RecentsView(navController = navController)
    } else {
        GetStartedView(navController = navController)
    }
}