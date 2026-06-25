package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.data.BackupRestoreImpl
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.models.BackupRestoreEnt
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.FileOutputStream

sealed class BackupRestoreUiStates {
    object Idle : BackupRestoreUiStates()
    object Loading : BackupRestoreUiStates()
    object Success : BackupRestoreUiStates()
    data class Error(val message: String) : BackupRestoreUiStates()
}


@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {

    private val _uiState = MutableStateFlow<BackupRestoreUiStates>(BackupRestoreUiStates.Idle)
    val uiState: StateFlow<BackupRestoreUiStates> = _uiState

    private val _recoveryKeyUiState = MutableStateFlow<List<String>>(mutableListOf())
    val recoveryKeyUiState: StateFlow<List<String>> = _recoveryKeyUiState

    val db = Datastore.getDatastore(context)?.backupRestoreDao()
        ?: throw Exception("Failed to open database")

    fun saveUri(uri: Uri?) {
        if(uri == null) return

        _uiState.value = BackupRestoreUiStates.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val recoveryKey = writeBackup(uri)
                db.insert(BackupRestoreEnt(
                    uri = uri.toString(),
                    recovery_key = recoveryKey
                ))
                _uiState.value = BackupRestoreUiStates.Idle
            } catch(e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupRestoreUiStates.Error(e.message
                    ?: "Error saving backupRestore")
            }
        }
    }

    private suspend fun writeBackup(uri: Uri): ByteArray {
        try {
            val backup = BackupRestoreImpl(context).backup()
            val sBackup = backup.serialize()
            with(context.contentResolver.openFileDescriptor(uri, "w")) {
                this?.fileDescriptor.let { fd ->
                    val fileOutputStream = FileOutputStream(fd)
                    fileOutputStream.write(sBackup)
                    // Let the document provider know you're done by closing the stream.
                    fileOutputStream.close()
                }
                this?.close()
            }
            return backup.getRecoveryKey() ?: throw Exception("Expected recovery key")
        } catch(e: Exception)  {
            e.printStackTrace()
            throw e
        }
    }

    fun readBackup(uri: Uri, recoveryKey: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val contents = try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                }
            } catch (e: Exception) {
                throw e
            }
            contents ?: throw Exception("Invalid backup file")

            try {
                BackupRestoreImpl(context)
                    .restore(contents, recoveryKey)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun showRecoveryKey() {
        viewModelScope.launch {
            val backupRestore = db.fetch()
            _recoveryKeyUiState.value = convertAndSplitBytes(backupRestore?.recovery_key
                ?: throw Exception("No recovery key found"))
        }
    }

    private fun convertAndSplitBytes(bytes: ByteArray): List<String> {
        // Convert bytes to a UTF-8 string
        val fullString = String(bytes, Charsets.UTF_8)

        // Optional: Filter out whitespace/newlines if you only want visible characters
         val cleanedString = fullString.filter { !it.isWhitespace() }

        // Chunk the string into substrings of 4 characters each, and take the first 4 chunks
        return cleanedString.chunked(4)
    }
}