package com.example.sw0b_001.data

import android.content.Context
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.extensions.generateRandomBytes
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor

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
                val timestamp = (System.currentTimeMillis() / 1000).toString()
                val nonce = context.generateRandomBytes(16)
                val methodName = "/${method?.fullMethodName}"
                val llt = String(VaultsGrpcImpl(context).fetchLongLivedToken()!!,
                    Charsets.UTF_8)

                val requestString = methodName.encodeToByteArray() +
                        timestamp.encodeToByteArray() +
                        nonce
                val signature = VaultsGrpcImpl(context).signGrpcRequest(requestString)

                val sigKey = Metadata.Key.of("X-Sig-bin", Metadata.BINARY_BYTE_MARSHALLER)
                val tsKey = Metadata.Key.of("X-Timestamp", Metadata.ASCII_STRING_MARSHALLER)
                val nonceKey = Metadata.Key.of("X-Nonce-bin", Metadata.BINARY_BYTE_MARSHALLER)
                val bearer = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)

                headers?.put(bearer, "Bearer $llt")
                headers?.put(sigKey, signature)
                headers?.put(tsKey, timestamp)
                headers?.put(nonceKey, nonce)
                super.start(responseListener, headers)
            }
        }
    }
}