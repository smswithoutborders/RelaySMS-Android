package com.example.sw0b_001.ui.modals

import android.telephony.PhoneNumberUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arpitkatiyarprojects.countrypicker.CountryPickerOutlinedTextField
import com.arpitkatiyarprojects.countrypicker.enums.CountryListDisplayType
import com.arpitkatiyarprojects.countrypicker.models.CountryDetails
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.SupportedPlatforms
import com.example.sw0b_001.ui.viewModels.PnbaUiState
import com.example.sw0b_001.ui.viewModels.TokensUiState
import com.example.sw0b_001.ui.viewModels.TokensViewModel


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PNBAPhoneNumberCodeRequestView(
    tokensViewModel: TokensViewModel,
    showModal: Boolean,
    platform: SupportedPlatforms,
    onDismissRequest: () -> Unit,
) {
    var selectedCountry by remember { mutableStateOf<CountryDetails?>(null) }

    var phoneNumber by remember { mutableStateOf("") }
    var authCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage: String? by remember { mutableStateOf(null) }

    val buttonRequester = remember { BringIntoViewRequester() }

    val uiState by tokensViewModel.isStoringUiState.collectAsStateWithLifecycle()
    val pnbaUiState by tokensViewModel.pnbaUiState.collectAsStateWithLifecycle()

    var isEnabled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(pnbaUiState) {
        when(val state = pnbaUiState) {
            PnbaUiState.Success -> {
                tokensViewModel.clearStoringState()
                onDismissRequest()
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            TokensUiState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is TokensUiState.Error -> {
                isLoading = false
                errorMessage = state.exception.message
            }
            else -> {
                isLoading = false
                errorMessage = null
            }
        }

    }

    LaunchedEffect(isLoading) {
        isEnabled = !isLoading
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false,
    )
    if (showModal) {
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

                errorMessage?.let {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Text(
                            errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                    }
                }

                when(val state = pnbaUiState) {
                    PnbaUiState.PhoneNumberRequested -> {
                        PhoneNumberRequestedView(
                            phoneNumber = phoneNumber,
                            enabled = isEnabled,
                            onPhoneNumberChangedCallback = { cc, pn ->
                                selectedCountry = cc
                                phoneNumber = pn
                            }
                        )
                    }
                    PnbaUiState.AuthCodeRequested -> {
                        AuthenticationCodeRequestedView(
                            authCode = authCode,
                            displayName = platform.display_name,
                            enabled = isEnabled,
                            onAuthCodeChangeCallback = { authCode = it }
                        )
                    }
                    PnbaUiState.PasswordRequested -> {
                        PasswordRequestedView(
                            password = password,
                            enabled = isEnabled,
                            onPasswordChangedCallback = { password = it }
                        )
                    }
                    else -> {}
                }

                Button(
                    onClick = {
                        val phoneNumber1 = selectedCountry!!.countryPhoneNumberCode + phoneNumber
                        when(val state = pnbaUiState) {
                            PnbaUiState.PhoneNumberRequested -> {
                                tokensViewModel.store(
                                    platform = platform,
                                    phoneNumber = phoneNumber1,
                                )
                            }
                            PnbaUiState.AuthCodeRequested -> {
                                tokensViewModel.store(
                                    platform = platform,
                                    phoneNumber = phoneNumber1,
                                    authCode = authCode
                                )
                            }
                            PnbaUiState.PasswordRequested -> {
                                tokensViewModel.store(
                                    platform = platform,
                                    phoneNumber = phoneNumber1,
                                    authCode = authCode,
                                    password = password
                                )
                            }
                            else -> {}
                        }
                    },
                    enabled = isEnabled && when(val state = pnbaUiState) {
                        PnbaUiState.PhoneNumberRequested -> {
                            if(selectedCountry == null) false
                            else {
                                val phoneNumber1 = selectedCountry!!.countryPhoneNumberCode + phoneNumber
                                PhoneNumberUtils.isWellFormedSmsAddress(phoneNumber1)
                            }
                        }
                        PnbaUiState.AuthCodeRequested -> { authCode.length > 3 }
                        PnbaUiState.PasswordRequested -> { password.isNotEmpty() }
                        else -> true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 24.dp)
                        .bringIntoViewRequester(buttonRequester),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                ) {
                    if(isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                    else {
                        Text(stringResource(R.string.submit))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PasswordRequestedView(
    password: String = "",
    enabled: Boolean = true,
    onPasswordChangedCallback: (String) -> Unit = {},
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChangedCallback,
            label = { Text(text = stringResource(R.string.password), style = MaterialTheme.typography.bodySmall) },
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

                val description = if (isPasswordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)

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
            enabled = enabled
        )
    }

}


@Preview(showBackground = true)
@Composable
private fun AuthenticationCodeRequestedView(
    authCode: String = "",
    displayName: String = "",
    enabled: Boolean = false,
    onAuthCodeChangeCallback: (String) -> Unit = {},
) {
    var isCodeVisible by remember { mutableStateOf(false) }
    Column(Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = authCode,
            onValueChange = onAuthCodeChangeCallback,
            label = { Text(text = stringResource(R.string.code), style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isCodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = {
                Text(
                    stringResource(
                        R.string.please_enter_your_avoid_copying_and_pasting_if_telegram_to_not_get_flagged,
                        displayName
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            },
            trailingIcon = {
                val image = if (isCodeVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (isCodeVisible) stringResource(R.string.hide_password) else stringResource(
                    R.string.show_password
                )

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
            enabled = enabled
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun PhoneNumberRequestedView(
    phoneNumber: String = "",
    enabled: Boolean = false,
    onPhoneNumberChangedCallback: (CountryDetails, String) -> Unit = { cc, pn ->},
) {
    var selectedCountry by remember { mutableStateOf<CountryDetails?>(null) }

    Column(Modifier.padding(8.dp)) {
        CountryPickerOutlinedTextField(
            mobileNumber = phoneNumber,
            onMobileNumberChange = {
                onPhoneNumberChangedCallback(selectedCountry!!, it)
            },
            onCountrySelected = { selectedCountry = it },
            defaultCountryCode = "cm",
            countryListDisplayType = CountryListDisplayType.Dialog,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            label = { Text(stringResource(R.string.phone_number), style = MaterialTheme.typography.bodySmall) },
            enabled = enabled
        )
    }
}