package com.example.sw0b_001.ui.views.addAccounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.arpitkatiyarprojects.countrypicker.CountryPickerOutlinedTextField
import com.arpitkatiyarprojects.countrypicker.enums.CountryListDisplayType
import com.arpitkatiyarprojects.countrypicker.models.CountryDetails
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.ui.modals.PlatformOptionsModal
import com.example.sw0b_001.ui.theme.AppTheme


@Composable
fun PNBAPhoneNumberCodeRequestView(
    platform: AvailablePlatforms? = null,
    isPhoneNumberRequested: Boolean = true,
    isAuthenticationCodeRequested: Boolean,
    isPasswordRequested: Boolean,
) {
    var isCodeVisible by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf<CountryDetails?>(null) }

    var phoneNumber by remember { mutableStateOf("") }
    var authCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        platform?.let {
            if(it.name == "telegram" && isAuthenticationCodeRequested) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        "Please enter your Telegram code without copying it from the message - copying might get flagged and Telegram might block your account.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if(isPhoneNumberRequested) {
            CountryPickerOutlinedTextField(
                mobileNumber = phoneNumber,
                onMobileNumberChange = { phoneNumber = it },
                onCountrySelected = {
                    selectedCountry = it
                },
                defaultCountryCode = "cm",
                countryListDisplayType = CountryListDisplayType.Dialog,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                label = {
                    Text(
                        text = "Phone Number",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                enabled = true
            )
        }
        if(isAuthenticationCodeRequested) {
            OutlinedTextField(
                value = authCode,
                onValueChange = { authCode = it },
                label = { Text(text = "Code", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isCodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (isCodeVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (isCodeVisible) "Hide password" else "Show password"

                    IconButton(onClick = { isCodeVisible = !isCodeVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                enabled = true
            )
        }
        if(isPasswordRequested) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (isPasswordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (isPasswordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                enabled = true
            )
        }

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
                .padding(top=32.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            Text("Submit")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PNBAPreview() {
    AppTheme(darkTheme = false) {
        val platform = AvailablePlatforms(
            name = "telegram",
            shortcode = "g",
            service_type = "email",
            protocol_type = "oauth2",
            icon_png = "",
            icon_svg = "",
            support_url_scheme = true,
            logo = null
        )

        PNBAPhoneNumberCodeRequestView(
            platform = platform,
            isPhoneNumberRequested = true,
            isPasswordRequested = true,
            isAuthenticationCodeRequested = true
        )
    }
}
