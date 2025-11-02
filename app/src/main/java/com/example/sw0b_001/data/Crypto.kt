package com.example.sw0b_001.data

import android.util.Base64
import com.macasaet.fernet.Key
import com.macasaet.fernet.StringValidator
import com.macasaet.fernet.Token
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.security.PublicKey
import java.security.cert.Certificate
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection


object Crypto {
    fun getUrlPublicKey(urlInput: String): PublicKey {
        val urlBuild = URL(urlInput)
        val url = URL("${urlBuild.protocol}://${urlBuild.host}")
        val urlConnection = url.openConnection() as HttpsURLConnection
        val tmp: InputStream = BufferedInputStream(urlConnection.inputStream)
        val certificate: Array<Certificate> = urlConnection.serverCertificates
        return certificate[0].publicKey
    }

    fun convertToPemFormat(key: ByteArray?): String {
        var keyString: String = Base64.encodeToString(key, Base64.DEFAULT)
        keyString = "-----BEGIN PUBLIC KEY-----\n$keyString"
        keyString += "-----END PUBLIC KEY-----"
        return keyString
    }

    fun decryptFernet(key: ByteArray, data: String): String {
        val fKey = Key(key)
        val token = Token.fromString(data)
        val validator = object : StringValidator { }
        return token.validateAndDecrypt(fKey, validator)
    }

    fun HMAC(secretKey: ByteArray, data: ByteArray): ByteArray {
        val algorithm = "HmacSHA256"
        val hmacOutput = Mac.getInstance(algorithm)
        val key: SecretKey = SecretKeySpec(secretKey, algorithm)
        hmacOutput.init(key)
        hmacOutput.update(data)
        return hmacOutput.doFinal()
    }
}