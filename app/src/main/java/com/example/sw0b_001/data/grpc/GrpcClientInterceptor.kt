package com.example.sw0b_001.data.grpc

import android.content.Context
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.example.sw0b_001.extensions.context.getStaticKeys
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import uniffi.relaysms_spec_payload.v1RequestsEncrypt

class GrpcClientInterceptor(
    private val context: Context,
    private val onRequest: () -> ByteArray?,
): ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT?, RespT?>?,
        callOptions: CallOptions?,
        next: Channel?
    ): ClientCall<ReqT?, RespT?> {

        return object: ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next?.newCall(method, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT?>?, headers: Metadata?) {
                val methodName = "/${method?.fullMethodName}"

                val protocol = Protocols(context)
                protocol.generateDH().use { key ->
                    val keyId = (0 until 256).random()
                    val authenticationPublicKey = context.getStaticKeys(keyId)
                        ?: throw Exception("Could not find static keys for id")

                    val payload = onRequest()

                    val ciphertext = v1RequestsEncrypt(
                        ec = key.privateKey!!,
                        ssKidPk = authenticationPublicKey,
                        methodName = methodName.toByteArray(),
                        payload = payload
                    )

                    /**
                     * X-Payload-bin: <ciphertext>
                     * X-Public-Key-bin: <eC_pk>
                     * X-Key-ID: <Key-ID>
                     * X-Nonce: <Nonce>
                     * X-Timestamp: <Timestamp>
                     */
                    val xPayloadBin = Metadata.Key.of("X-Payload-bin",
                        Metadata.BINARY_BYTE_MARSHALLER)
                    val xPublicKeyBin = Metadata.Key.of("X-Public-Key-bin",
                        Metadata.BINARY_BYTE_MARSHALLER)
                    val xKeyId = Metadata.Key.of("X-Key-ID",
                        Metadata.ASCII_STRING_MARSHALLER)
                    val xNonce = Metadata.Key.of("X-Nonce-bin",
                        Metadata.BINARY_BYTE_MARSHALLER)
                    val xTimestamp = Metadata.Key.of("X-Timestamp",
                        Metadata.ASCII_STRING_MARSHALLER)
                    headers?.put(xPayloadBin, ciphertext.ciphertext)
                    headers?.put(xPublicKeyBin, key.publicKey.copyOf())
                    headers?.put(xKeyId, keyId.toString())
                    headers?.put(xNonce, ciphertext.nonce)
                    headers?.put(xTimestamp, ciphertext.timestamp.toString())

                    super.start(responseListener, headers)
                }
            }
        }
    }
}