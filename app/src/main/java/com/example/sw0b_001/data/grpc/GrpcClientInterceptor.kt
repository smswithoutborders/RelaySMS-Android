package com.example.sw0b_001.data.grpc

import com.example.sw0b_001.data.models.Keys
import com.example.sw0b_001.extensions.context.getStaticKeys
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import uniffi.relaysms_spec_payload.v1TokenEncrypt

class GrpcClientInterceptor(
    private val service: GrpcInterface,
    private val tokenHash: ByteArray
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
                val keyId = (0 until 256).random()
                val authenticationPublicKey = service.getContext()
                    .getStaticKeys(keyId)
                    ?: throw Exception("Could not find static keys for id")
                val ecKid = Keys.getKey(
                    service.getContext(),
                    tokenHash,
                    keyId.toUByte()
                )
                ecKid.use { ecKid ->
                    val token = v1TokenEncrypt(
                        ecKid = ecKid.privateKey,
                        ssKidPk = authenticationPublicKey,
                        esKidPk = ecKid.publicKey,
                        methodName = methodName.encodeToByteArray(),
                        keyId = keyId.toUByte(),
                    )
                    try {
                        val xKeyId = Metadata.Key.of("X-Key-id", Metadata.ASCII_STRING_MARSHALLER)
                        val bearer = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)

                        headers?.put(bearer, "Bearer $token")
                        headers?.put(xKeyId, keyId.toString())
                        super.start(responseListener, headers)
                    } finally {
                        token.fill(0)
                    }
                }
            }
        }
    }
}