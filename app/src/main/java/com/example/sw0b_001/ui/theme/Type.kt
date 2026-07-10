package com.example.sw0b_001.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font as GoogleFontLoader
import com.example.sw0b_001.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val bodyFontFamily = FontFamily(
    GoogleFontLoader(
        googleFont = GoogleFont("Roboto"),
        fontProvider = provider,
    )
)

val UnboundedFontFamily = FontFamily(
    Font(R.font.unbounded_regular, FontWeight.Normal),
    Font(R.font.unbounded_semibold, FontWeight.Bold)
)

val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = UnboundedFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = UnboundedFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = UnboundedFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = UnboundedFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = UnboundedFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = UnboundedFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = UnboundedFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = UnboundedFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = UnboundedFontFamily),

    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)

