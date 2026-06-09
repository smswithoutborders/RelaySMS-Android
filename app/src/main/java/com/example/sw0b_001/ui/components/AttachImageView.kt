package com.example.sw0b_001.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afkanerd.lib_image_android.R
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun AttachImageView(
    image: Bitmap,
    onCancelCallback: (() -> Unit)?,
    onClickCallback: () -> Unit,
) {
    Card(
        onClick = onClickCallback,
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Column {
                Image(
                    bitmap = image.asImageBitmap(),
                    "",
                    modifier = Modifier
                        .size(150.dp),
                    contentScale = ContentScale.FillWidth
                )
            }
            onCancelCallback?.let {
                Column {
                    IconButton(
                        onClick = onCancelCallback
                    ) {
                        Text(
                            "X",
                            fontFamily = FontFamily.SansSerif,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AttachImageView_Preview() {
    AppTheme {
        val context = LocalContext.current
        val bitmap = BitmapFactory.decodeResource(context.resources,
            R.drawable._0241226_124819)
        AttachImageView(
            bitmap,
            {}
        ) {

        }
    }
}
