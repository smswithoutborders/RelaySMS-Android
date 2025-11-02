package com.example.sw0b_001.data

import android.content.Context
import android.util.Base64
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.preference.PreferenceManager
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Headers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Ratchets
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.afkanerd.smswithoutborders_libsmsmms.extensions.toLittleEndianBytes
import com.example.sw0b_001.data.Helpers.toBytes
import java.nio.charset.StandardCharsets

class RatchetComposerFormatter(
    val context: Context,
    val state: States,
    val AD: ByteArray?
) {
    init {
        if(state.DHs == null) {
            val SK = Publishers.fetchPublisherSharedKey(context)
            Ratchets.ratchetInitAlice(state, SK, AD)
        }
    }

    fun decryptBridge(
        header: Headers,
        content: ByteArray,
    ): String {
        val text = Ratchets.ratchetDecrypt(state, header, content, AD)
        return String(text, Charsets.UTF_8)
    }
}


