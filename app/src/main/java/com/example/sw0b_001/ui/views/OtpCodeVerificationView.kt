package com.example.sw0b_001.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun OtpCodeVerificationView(
    onCodeSubmitted: (String) -> Unit = {},
    onResendClicked: () -> Unit = {},
    navController: NavController
) {
    var otpCode by remember { mutableStateOf("") }
    var timeLeft by remember { mutableLongStateOf(60L) }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Input Your Code",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please enter the 6-digit code we sent to you via SMS",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            OtpCodeInputField(
                otpCode = otpCode,
                onOtpCodeChanged = {
                    if (it.length <= 6) {
                        otpCode = it
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "The code would automatically be detected in some cases",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) ,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (otpCode.length == 6) {
                        navController.navigate(HomepageScreen)
                    }
                },
                enabled = otpCode.length == 6,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Time left until code expires: 60s",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (timeLeft == 0L){
                Button(onClick = onResendClicked) {
                    Text(text = "Resend Code")
                }
            }
        }
    }
}

@Composable
fun OtpCodeInputField(otpCode: String, onOtpCodeChanged: (String) -> Unit) {
    BasicTextField(
        value = otpCode,
        onValueChange = onOtpCodeChanged,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(6) { index ->
                    val char = when {
                        index >= otpCode.length -> ""
                        else -> otpCode[index].toString()
                    }
                    OtpCodeDigitBox(char = char)
                }
            }
        }
    )
}

@Composable
fun OtpCodeDigitBox(char: String) {
    Text(
        text = char,
        modifier = Modifier
            .width(40.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        style = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


@Preview(showBackground = true)
@Composable
fun OtpCodeVerificationPreview() {
    AppTheme {
        OtpCodeVerificationView(
            navController = NavController(context = LocalContext.current)
        )
    }
}