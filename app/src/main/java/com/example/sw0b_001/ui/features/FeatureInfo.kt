package com.example.sw0b_001.ui.features

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.sw0b_001.R

data class FeatureInfo(
    val id: String, // Unique ID for this feature (e.g. "alias_auth_v1", "new_theme_picker_v1")
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int? = null,
)

object AppFeatures {
    val ALL_FEATURES = listOf(
        FeatureInfo(
            id = "oauth_token_storage_device_setting_info_alert",
            titleRes = R.string.new_feature_title,
            descriptionRes = R.string.oauth_token_storage_device_setting_info,
            iconRes = R.drawable.relaysms_icon_default_shape
        ),

    )
}