package com.example.sw0b_001.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri


class SMSHandler {
    companion object {
        fun transferToDefaultSMSApp(context: Context, gatewayClientMSISDN: String,
                                    encryptedContent: String?): Intent {
            val intent = Intent().apply {
                type = "text/plain"
                action = Intent.ACTION_SENDTO
                setData(Uri.parse("smsto:$gatewayClientMSISDN"))
                putExtra("sms_body", encryptedContent)
            }
            val possibleActivitiesList: List<ResolveInfo> = context.packageManager
                    .queryIntentActivities(intent, PackageManager.MATCH_ALL)

            // Verify that an activity in at least two apps on the user's device
            // can handle the intent. Otherwise, start the intent only if an app
            // on the user's device can handle the intent.

            // Verify that an activity in at least two apps on the user's device
            // can handle the intent. Otherwise, start the intent only if an app
            // on the user's device can handle the intent.
            if (possibleActivitiesList.size > 1) {

                // Create intent to show chooser.
                // Title is something similar to "Share this photo with."
                val title: String = context.resources
                        .getString(com.example.sw0b_001.R.string.choose_sms_app)
                return Intent.createChooser(intent, title)
            } else if (intent.resolveActivity(context.packageManager) == null) {
                throw Exception("No package to manage that")
            }
            return intent
        }
    }
}
