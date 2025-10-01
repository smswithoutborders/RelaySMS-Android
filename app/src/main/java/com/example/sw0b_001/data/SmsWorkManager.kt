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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.ceil

class SmsWorkManager(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams ) {

    private lateinit var messageStateChangedBroadcast: BroadcastReceiver

    val workValue = MutableStateFlow<Result?>(null)

    override suspend fun doWork(): Result {
        val formattedPayload = inputData.getByteArray(FORMATTED_SMS_PAYLOAD)
            ?: return Result.failure()

        val itp = inputData.getString(ITP_PAYLOAD).let {
            if(it == null) return Result.failure()
            Json.decodeFromString<ImageTransmissionProtocol>(it)
        }

        val dividedPayload = try {
            divideImagePayload(
                formattedPayload,
                itp
            )
        } catch(e: Exception) {
            e.printStackTrace()
        }

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

    private val segmentSize: Int = 3

    @Throws
    private fun divideImagePayload(
        payload: ByteArray,
        imageTransmissionProtocol: ImageTransmissionProtocol,
    ): MutableList<ByteArray> {
        var encodedPayload = payload
        val standardSegmentSize = 150 * segmentSize
        val dividedImage = mutableListOf<ByteArray>()

        var segmentNumber = 0
        do {
            var metaData = byteArrayOf(
                imageTransmissionProtocol.version,
                imageTransmissionProtocol.sessionId,
                imageTransmissionProtocol.getSegNumberNumberSegment(segmentNumber),
            )
            if(segmentNumber == 0) {
                metaData +=
                    imageTransmissionProtocol.imageLength.toByteArray() +
                            imageTransmissionProtocol.textLength.toByteArray()
            }

            val size = (standardSegmentSize - metaData.size)
                .coerceAtMost(encodedPayload.size)
            val buffer = metaData +  encodedPayload.take(size).toByteArray()
            if(buffer.size > standardSegmentSize) {
                throw Exception("Buffer size > $standardSegmentSize")
            }
            encodedPayload = encodedPayload.drop(buffer.size).toByteArray()

            segmentNumber += 1
            if(segmentNumber >= 256 / 2) {
                throw Exception("Segment number > ${256 /2 }")
            }

            dividedImage.add(buffer)
        } while(encodedPayload.isNotEmpty())

        return dividedImage
    }

    companion object {
        const val ITP_PAYLOAD = "ITP_PAYLOAD"
        const val FORMATTED_SMS_PAYLOAD = "FORMATTED_SMS_PAYLOAD"
    }

}