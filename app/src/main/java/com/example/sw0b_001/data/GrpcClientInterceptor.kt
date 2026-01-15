package com.example.sw0b_001.data

import android.content.Context
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.CryptoHelpers
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import java.security.MessageDigest

class GrpcClientInterceptor(val context: Context) : ClientInterceptor{
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT?, RespT?>?,
        callOptions: CallOptions?,
        next: Channel?
    ): ClientCall<ReqT?, RespT?> {

        return object: ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next?.newCall(method, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT?>?, headers: Metadata?) {
                val timestamp = System.currentTimeMillis().toString()
                val nonce = CryptoHelpers.generateRandomBytes(16)
                val methodName = method?.fullMethodName
                val llt = Base64.encodeToString(
                    Vaults(context).fetchLongLivedToken(), Base64.DEFAULT)

                val digest = MessageDigest.getInstance("SHA-256")
                digest.update(methodName?.encodeToByteArray() ?: byteArrayOf())
                digest.update(timestamp.encodeToByteArray())
                digest.update(nonce)
                val signature = digest.digest()

                val sigKey = Metadata.Key.of("X-Sig", Metadata.ASCII_STRING_MARSHALLER)
                val tsKey = Metadata.Key.of("X-Timestamp", Metadata.ASCII_STRING_MARSHALLER)
                val nonceKey = Metadata.Key.of("X-Nonce", Metadata.ASCII_STRING_MARSHALLER)
                val bearer = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)

                headers?.put(bearer, "Bearer $llt")
                headers?.put(sigKey, Base64.encodeToString(signature, Base64.DEFAULT))
                headers?.put(tsKey, timestamp)
                headers?.put(nonceKey, Base64.encodeToString(nonce, Base64.DEFAULT))
                super.start(responseListener, headers)
            }
        }
    }
}