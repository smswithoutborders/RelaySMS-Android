package com.example.sw0b_001.Models

class NavigationFlowHandler {
    var loginSignupPhoneNumber: String = ""
    var loginSignupPassword: String = ""

    var navigationCompleteCallback: () -> Unit = {}
}