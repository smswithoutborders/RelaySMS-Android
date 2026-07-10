package com.example.sw0b_001.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedCard
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults



data class OnboardingPageData(
    val illustration: Painter,
    val title: String,
    val description: String,

    val cardTitle: String? = null,
    val cardDescription: String? = null,
    val cardIcon: Painter? = null,

    val buttonText: String? = null,
    val buttonAction: (() -> Unit)? = null
)

@Composable
fun OnboardingPage(
    data: OnboardingPageData,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = data.illustration,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(.65f)
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineSmall.copy(

                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyLarge.copy(

                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (data.cardTitle != null) {

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(18.dp),

                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(.35f)
                ),

                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(.18f)
                )

            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        data.cardIcon?.let {

                            Image(
                                painter = it,
                                contentDescription = null,

                                colorFilter = ColorFilter.tint(
                                    MaterialTheme.colorScheme.primary
                                ),

                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Text(
                            text = data.cardTitle,
                            style = MaterialTheme.typography.titleMedium.copy(

                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = data.cardDescription ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(

                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (data.buttonText != null) {

            Button(
                onClick = {
                    data.buttonAction?.invoke()
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                shape = RoundedCornerShape(40.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )

            ) {

                Text(
                    text = data.buttonText,

                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
    }

}



