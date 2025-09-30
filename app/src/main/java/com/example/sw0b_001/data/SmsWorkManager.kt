package com.example.sw0b_001.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.autofill.ImageTransformation
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.afkanerd.smswithoutborders_libsmsmms.receivers.SmsTextReceivedReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.takeWhile
import kotlin.math.ceil

class SmsWorkManager(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams ) {

    private lateinit var messageStateChangedBroadcast: BroadcastReceiver

    val workValue = MutableStateFlow<Result?>(null)

    override suspend fun doWork(): Result {
        val messagePayload = inputData.getString(SMS_WORK_MANAGER_PAYLOAD)

        // TODO: send the sms payload
        // TODO: wait for the incoming broadcast to inform if the message has been sent or failed

        handleBroadcast()

        workValue.first { it != null }

        return workValue.value!!
    }

    /**
     *  Multiple incoming broadcast are expected at this point.
     *  Next message should only begin going out if previous one sends.
     */
    private fun handleBroadcast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsTextReceivedReceiver.SMS_SENT_BROADCAST_INTENT)
        messageStateChangedBroadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null && intentFilter.hasAction(intent.action)) {
                }
            }
        }
        ContextCompat.registerReceiver(
            applicationContext,
            messageStateChangedBroadcast,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    companion object {
        const val SMS_WORK_MANAGER_PAYLOAD = "SMS_WORK_MANAGER_PAYLOAD"


    }

    private fun divideImagePayload(isBridge: Boolean): MutableList<ByteArray> {
        val standardSegmentSize = 150f
        val firstSegmentSize = 7
        val secondarySegmentSize = 5
        val dividedImage = mutableListOf<ByteArray>()
        val numSegments = ceil(image.size / standardSegmentSize) +
                (firstSegmentSize + secondarySegmentSize)

        for(i in 0..numSegments.toInt()) {
            val segment = if(i == 0) {
                // 7 bytes = header size
                image.take(standardSegmentSize.toInt() - firstSegmentSize)
            } else {
                // 5 bytes = header size
                image.take(standardSegmentSize.toInt() - secondarySegmentSize)
            }
            dividedImage.add(segment.toByteArray())
        }
        return dividedImage
    }
}