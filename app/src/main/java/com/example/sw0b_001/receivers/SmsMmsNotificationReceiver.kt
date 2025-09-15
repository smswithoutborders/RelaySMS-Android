package com.example.sw0b_001.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.impl.utils.ForceStopRunnable
import com.afkanerd.lib_smsmms_android.R
import com.afkanerd.smswithoutborders_libsmsmms.data.entities.Conversations
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.NotificationTxType
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDatabase
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.notify
import com.afkanerd.smswithoutborders_libsmsmms.receivers.SmsTextReceivedReceiver
import com.example.sw0b_001.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsMmsNotificationReceiver : BroadcastReceiver () {

    private val cls = MainActivity::class.java

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            SmsTextReceivedReceiver.SMS_SENT_BROADCAST_INTENT_LIB -> {
                val id = intent.getLongExtra("id", -1)
                val self = intent.getBooleanExtra("self", false)
                val type = intent.getStringExtra("type")

                CoroutineScope(Dispatchers.IO).launch {
                    context?.getDatabase()?.conversationsDao()
                        ?.getConversation(id)?.let { conversation ->
                            when(conversation.sms?.status) {
                                Telephony.Sms.STATUS_FAILED -> {
                                    notifyMessageFailedToSend(context, conversation)
                                }
                                else -> {
                                    context.notify(
                                        conversation = conversation,
                                        cls = cls,
                                        self = self,
                                    )
                                }
                            }
                        }
                }
            }
        }
    }

    private fun notifyMessageFailedToSend(context: Context, conversation: Conversations) {
        val content = context
            .getString(
                R.string
                    .message_failed_send_notification_description_a_message_failed_to_send_to) +
                " ${conversation.sms?.address}"

        context.notify(
            conversation = conversation,
            actions = false,
            text = content,
            cls = cls,
        )
    }

}