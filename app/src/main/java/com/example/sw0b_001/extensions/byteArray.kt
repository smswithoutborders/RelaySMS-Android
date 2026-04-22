package com.example.sw0b_001.extensions

import java.security.MessageDigest

fun ByteArray.sha256(): ByteArray {
    val ho = MessageDigest.getInstance("SHA-256")
    return ho.digest(this)
}
