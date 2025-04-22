package com.example.sw0b_001.Models

import com.example.sw0b_001.ui.views.BottomTabsItems
import com.example.sw0b_001.ui.views.OTPCodeVerificationType

class NavigationFlowHandler {
    var loginSignupPhoneNumber: String = ""
    var loginSignupPassword: String = ""
    var countryCode: String = ""
    var otpRequestType: OTPCodeVerificationType? = null
    var nextAttemptTimestamp: Int? = null
}