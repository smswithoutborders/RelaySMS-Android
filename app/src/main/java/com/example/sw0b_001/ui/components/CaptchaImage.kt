package com.example.sw0b_001.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.fonts.FontStyle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableOpenTarget
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun CaptchaImage(
    bitmap: Bitmap,
    onDismissCallback: () -> Unit,
    onSubmitCallback: (String) -> Unit,
) {
    var answer by remember{ mutableStateOf("") }
    Dialog(
        onDismissRequest = onDismissCallback,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier
            .fillMaxWidth()
            .height(350.dp)
            .background(color = MaterialTheme.colorScheme.onPrimary)
        ) {
            Column( Modifier
                .padding(16.dp)
                .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image( bitmap.asImageBitmap(),
                    "",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.size(150.dp)
                )

                Spacer(Modifier.padding(16.dp))
                Text(
                    stringResource(R.string.enter_the_text_in_the_image),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(Modifier.padding(4.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = {answer = it},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.padding(8.dp))

                Button(onClick = {onSubmitCallback(answer)}, Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.submit1))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaptchaImage_Preview() {
    AppTheme {
        val context = LocalContext.current
        val bitmap = BitmapFactory.decodeResource(context.resources,
            R.drawable._0241226_124819)
        CaptchaImage(bitmap, {}) {}
    }
}

