package com.example.sw0b_001.data

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.sw0b_001.MainActivity
import java.io.File
import java.io.IOException
import java.util.Date

class CrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val logFile =
                File(context.getExternalFilesDir(null), "crash_${System.currentTimeMillis()}.log")
            logFile.writeText(buildString {
                appendLine("Time: ${Date()}")
                appendLine("Thread: ${thread.name}")
                appendLine("Stacktrace:")
                appendLine(Log.getStackTraceString(throwable))
            })
        } catch (e: Exception) {
            // avoid throwing inside the handler
            e.printStackTrace()
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        fun initialize(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            val customHandler = CrashHandler(context, defaultHandler)
            Thread.setDefaultUncaughtExceptionHandler(customHandler)

            // Clean up old crash logs (older than 7 days)
            Thread {
                val directory = context.getExternalFilesDir(null)
                val currentTime = System.currentTimeMillis()
                directory?.listFiles { file -> file.name.startsWith("crash_") }?.forEach { file ->
                    if (currentTime - file.lastModified() > 7 * 24 * 60 * 60 * 1000) {
                        file.delete()
                    }
                }
            }.start()
        }

        fun offerCrashLogOptions(activity: MainActivity, context: Context) {
            val logDir = context.getExternalFilesDir(null) ?: return
            val crashFiles = logDir.listFiles { file ->
                file.isFile && file.name.startsWith("crash_") && file.name.endsWith(".log")
            }?.sortedBy { it.lastModified() }

            if (crashFiles.isNullOrEmpty()) {
                Log.w("CrashShare", "No crash logs found")
                return
            }

            val mergedFile = mergeCrashLogs(context, crashFiles) ?: return

            AlertDialog.Builder(context)
                .setTitle("Crash logs")
                .setItems(arrayOf("Share", "Save to device")) { _, which ->
                    when (which) {
                        0 -> shareCrashLog(context, mergedFile)
                        1 -> activity.launchSaveCrashLog(mergedFile)
                    }
                }
                .show()
        }

        fun mergeCrashLogs(context: Context, crashFiles: List<File>): File? {
            val logDir = context.getExternalFilesDir(null) ?: return null
            val mergedFile = File(logDir, "merged_crash_logs.log")
            return try {
                mergedFile.bufferedWriter().use { writer ->
                    crashFiles.forEach { file ->
                        writer.appendLine("===== ${file.name} =====")
                        writer.appendLine(file.readText())
                        writer.appendLine()
                    }
                }
                mergedFile
            } catch (e: IOException) {
                Log.e("CrashShare", "Failed to merge crash logs", e)
                null
            }
        }

        private fun shareCrashLog(context: Context, logFile: File) {
            if (!logFile.exists()) {
                Log.w("CrashShare", "Log file does not exist: ${logFile.path}")
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                logFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Crash logs: ${logFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "Share crash logs")
            )
        }

        fun saveFileToUri(context: Context, sourceFile: File, destUri: Uri) {
            try {
                context.contentResolver.openOutputStream(destUri)?.use { outputStream ->
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Toast.makeText(context, "Crash logs saved", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Log.e("CrashSave", "Failed to save crash logs", e)
                Toast.makeText(context, "Failed to save crash logs", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
