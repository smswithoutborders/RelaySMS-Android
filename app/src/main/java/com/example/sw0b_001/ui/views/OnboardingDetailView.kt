package com.example.sw0b_001.ui.views

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun OnboardingDetailView(
    @DrawableRes imageRes: Int,
    description: String,
    useUnboundedFont: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(220.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun OnboardingDetailViewPreview() {
    AppTheme {
        OnboardingDetailView(
            imageRes = R.drawable.no_account,
            description = "RelaySMS no longer asks you to login or register because your privacy matters."
        )
    }
}