package com.example.sw0b_001.ui.views.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextComposeView(
    body: String,
    bodyCallback: (String) -> Unit,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(scrollState)
    ) {
        BasicTextField(
            value = body,
            onValueChange = { newValue ->
                bodyCallback(newValue)

                val lines = newValue.lines()
                val lineCount = lines.size

                val lineHeight = 20.dp
                val maxVisibleLines = 10

                if (lineCount > maxVisibleLines) {
                    val scrollOffset = with(density) {
                        (lineCount - maxVisibleLines) * lineHeight.toPx()
                    }
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollOffset.toInt())
                    }
                }
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            textStyle = TextStyle.Default.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            decorationBox = { innerTextField ->
                if (body.isEmpty()) {
                    Text(
                        text = stringResource(R.string.what_s_happening),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TextComposePreview() {
    AppTheme(darkTheme = false) {
        TextComposeView(
            "",
        ) {}
    }
}