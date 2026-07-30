package com.example.sw0b_001.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.smswithoutborders_libsmsmms.extensions.pulseLoading

@Preview(showBackground = true)
@Composable
fun PulsingMessagePlaceholder() {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = MaterialTheme.colorScheme.secondary
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .pulseLoading(isLoading = true, color)
        )

        Spacer(Modifier.padding(start=12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Long message line
            Row {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .pulseLoading(isLoading = true, color)
                )

                Spacer(Modifier.padding(start=32.dp))
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .pulseLoading(isLoading = true, color)
                        .weight(1f)
                )
            }
            // Short message line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .pulseLoading(isLoading = true, color)
            )
        }
    }
}